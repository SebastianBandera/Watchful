package app.alertify.entity;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.ManagedBean;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Entity(name = "Alert")
@Table(schema = "alert", name = "alerts")
@TypeDef(
	    typeClass = PostgreSQLIntervalType.class,
	    defaultForType = Duration.class
	)
@TypeDefs({
    @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Data
@Slf4j
@ManagedBean
public class Alert {

	  @Id
	  @GeneratedValue(strategy=GenerationType.IDENTITY)
	  private Long id;
	  
	  @Column(name="name", nullable = false)
	  private String name;

	  @Column(name="control", nullable = false)
	  private String control;

	  @Type(type = "jsonb")
	  @Column(name="params")
	  private String params;

	  @Column(name="periodicity", columnDefinition = "interval")
	  private Duration periodicity;
	  
	  @Column(name="active")
	  private boolean active;
	  
      //@OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
      //private List<AlertResult> alertResults;
	  
	  @SuppressWarnings("unchecked")
	  public Map<String,Object> getParametrosMap() {
			try {
				return new ObjectMapper().readValue(params, HashMap.class);
			} catch (Exception e) {
				e.printStackTrace();
				log.error("error getParametrosMap()", e);
				return new HashMap<>();
			}
	  }
	  
	  @Override
	  public boolean equals(Object o) {
	      if (this == o) return true;
	      if (o == null || getClass() != o.getClass()) return false;
	      Alert myEntity = (Alert) o;
	      return id != null && id.equals(myEntity.id);
	  }

	  @Override
	  public int hashCode() {
	      return Objects.hash(id);
	  }
}
