package edu.cnm.deepdive.join_me_backend.view;

import edu.cnm.deepdive.join_me_backend.model.entity.Person;
import edu.cnm.deepdive.join_me_backend.model.entity.Vertex;
import java.net.URI;
import java.util.List;

public interface BaseSquare {

//  List<Person> getPeople();

  List<Vertex> getVertices();

//  Vertex getNWVertex();
//
//  Vertex getNEVertex();
//
//  Vertex getSEVertex();
//
//  Vertex getSWVertex();

  int getId();

  URI getHref();

}