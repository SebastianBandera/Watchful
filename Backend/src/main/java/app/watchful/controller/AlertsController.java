package app.watchful.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.watchful.control.common.StringUtils;
import app.watchful.controller.dto.AlertDto;
import app.watchful.controller.dto.AlertResultDto;
import app.watchful.controller.dto.CodStatusDto;
import app.watchful.controller.dto.SimpleMapper;
import app.watchful.controller.dto.SimpleMapperTypeRelationsBuilder;
import app.watchful.entity.Alert;
import app.watchful.entity.AlertResult;
import app.watchful.entity.CodStatus;
import app.watchful.entity.repositories.AlertRepository;
import app.watchful.entity.repositories.AlertResultRepository;
import app.watchful.service.ThreadControl;
import app.watchful.startup.StartupProcess;

@RestController
public class AlertsController {
	
	@Value("${pageSize:#{10}}")
	private int pageSize;

	@Autowired
	private AlertRepository alertRepository;

	@Autowired
	private AlertResultRepository alertResultsRepository;
	
	@SuppressWarnings("unused")
	@Autowired
	private ThreadControl threadControl;
	
	@Autowired
	private StartupProcess startupProcess;
	
	@Autowired
	private SimpleMapper simpleMapper;
	
	@PostMapping("alerts/reload")
	public ResponseEntity<String> reload() {
		List<Alert> dbAlerts = alertRepository.findAll();
		
		dbAlerts.forEach(startupProcess::registerAlert);
		
		return ResponseEntity.ok(StringUtils.concat(String.valueOf(dbAlerts.size()), " alerts processed"));
	}

	@GetMapping("/alerts")
	public ResponseEntity<Page<AlertDto>> all(@RequestParam(defaultValue = "0") int page) {
		if (page < 0) return ResponseEntity.badRequest().build();
		return ResponseEntity.ok(alertRepository.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("id")))).map(in -> simpleMapper.map(in, AlertDto.class)));
	}

	@GetMapping("/alerts/results")
	public ResponseEntity<Page<AlertResultDto>> allResults(@RequestParam(defaultValue = "0") int page) {
		if (page < 0) return ResponseEntity.badRequest().build();
		return ResponseEntity.ok(alertResultsRepository.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("id")))).map(in -> simpleMapper.map(in, AlertResultDto.class, SimpleMapperTypeRelationsBuilder.newInstance().add(Alert.class, AlertDto.class).add(CodStatus.class, CodStatusDto.class).build())));
	}
	
	@GetMapping("/alerts/results/{id}/resolve")
	public ResponseEntity<Object> resolveResult(@PathVariable Long id) {
		if (id == null || id < 0) return ResponseEntity.badRequest().build();
		Optional<AlertResult> r = alertResultsRepository.findById(id);
		if (r.isPresent()) {
			AlertResult alertResult = r.get();
			if (alertResult.isNeeds_review()) {
				alertResult.setNeeds_review(false);
				alertResultsRepository.saveAndFlush(alertResult);
			}
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.notFound().build();
		}		
	}
	
}
