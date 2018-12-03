package edu.cnm.deepdive.join_me_backend.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.cnm.deepdive.join_me_backend.view.BaseVertex;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Entity
@Component
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vertex implements BaseVertex {

  private static EntityLinks entityLinks;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "vertex_id", nullable = false, updatable = false)
  private int id;

  @NonNull
  @Column(nullable = false)
  private double latitude;

  @NonNull
  @Column(nullable = false)
  private double longitude;

//  private List<Person> people;

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "vertices",
  cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
  @OrderBy("name ASC")
  private List<Square> squares = new LinkedList<>();

  @PostConstruct
  private void initEntityLinks(){
    String ignore = entityLinks.toString();
  }

  @Autowired
  private void setEntityLinks(EntityLinks entityLinks){
    Vertex.entityLinks = entityLinks;
  }

  @Override
  public double getLatitude() {
    return latitude;
  }

  @Override
  public double getLongitude() {
    return longitude;
  }

  @Override
  public List<Square> getSquares() {
    return squares;
  }

//  @Override
//  public List<Person> getPeople() {
//    return people;
//  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public URI getHref() {
    return entityLinks.linkForSingleResource(Vertex.class, id).toUri();
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public void setSquares(List<Square> squares) {
    this.squares = squares;
  }

//  public void setPeople(List<Person> people) {
//    this.people = people;
//  }
}