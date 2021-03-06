package edu.cnm.deepdive.join_me_backend.controller;


import edu.cnm.deepdive.join_me_backend.model.dao.InvitationRepository;
import edu.cnm.deepdive.join_me_backend.model.dao.PersonRepository;
import edu.cnm.deepdive.join_me_backend.model.dao.SquareRepository;
import edu.cnm.deepdive.join_me_backend.model.dao.VertexRepository;
import edu.cnm.deepdive.join_me_backend.model.entity.Invitation;
import edu.cnm.deepdive.join_me_backend.model.entity.Person;
import edu.cnm.deepdive.join_me_backend.model.entity.Square;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Person controller.
 */
@RestController
@ExposesResourceFor(Person.class)
@RequestMapping("/people")
public class PersonController {

  private InvitationRepository invitationRepository;
  private PersonRepository personRepository;
  private SquareRepository squareRepository;
  private VertexRepository vertexRepository;
  /**
   * The constant SOUTH_LAT_LINE.
   */
  public static double SOUTH_LAT_LINE = 35.0859000; //farthest south
  /**
   * The constant MIDDLE_LAT_LINE.
   */
  public static double MIDDLE_LAT_LINE = 35.0860000; //middle
  /**
   * The constant NORTH_LAT_LINE.
   */
  public static double NORTH_LAT_LINE = 35.0861000; //farthest north
  /**
   * The constant EAST_LONG_LINE.
   */
  public static double EAST_LONG_LINE = -106.6494000; //farthest east
  /**
   * The constant MIDDLE_LONG_LINE.
   */
  public static double MIDDLE_LONG_LINE = -106.6495000; //middle
  /**
   * The constant WEST_LONG_LINE.
   */
  public static double WEST_LONG_LINE = -106.6496000; //farthest west

  /**
   * Instantiates a new Person controller.
   *
   * @param invitationRepository the invitation repository
   * @param personRepository the person repository
   * @param squareRepository the square repository
   * @param vertexRepository the vertex repository
   */
  @Autowired
  public PersonController(
      InvitationRepository invitationRepository,
      PersonRepository personRepository,
      SquareRepository squareRepository,
      VertexRepository vertexRepository) {
    this.invitationRepository = invitationRepository;
    this.personRepository = personRepository;
    this.squareRepository = squareRepository;
    this.vertexRepository = vertexRepository;
  }

  /**
   * Gets all people in database.
   *
   * @return the list
   */
//
  @ApiOperation(value = "Gets all Person objects.", notes = "Retrieves all people.")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Person> list() {
    return personRepository.findAll();
  }

  /**
   * Adds a person to database.
   *
   * @param person the person
   * @return the response entity
   */
  @ApiOperation(value = "Adds person.", notes = "Adds person to database.")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Person> post(@ApiParam(value = "Partial person object.", required = true)@RequestBody Person person) {
    person.setClosestVertex(vertexRepository.findAll().get(0));
    long personId = personRepository.save(person).getPersonId();
    updateUser(person, personId);
    return ResponseEntity.created(person.getHref()).body(person);
  }

  /**
   * Get a person.
   *
   * @param personId the person id
   * @return the person
   */
  @ApiOperation(value = "Gets a person.", notes = "Retrieves a person according to personId.")
  @GetMapping(value = "{personId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Person get(@ApiParam(value = "Id for the person.", required = true)@PathVariable("personId") long personId) {
    return personRepository.findById(personId).get();
  }

  /**
   * Update a person.
   *
   * @param person the person
   * @param personId the person id
   * @return the response entity
   */
  @ApiOperation(value = "Updates a person.", notes = "Updates a person according to personId.")
  @PutMapping(value = "{personId}")
  public ResponseEntity<Person> updatePerson(@ApiParam(value = "Person object.", required = true)@RequestBody Person person,
      @ApiParam(value = "Id for the person.", required = true)@PathVariable("personId") long personId) {
    Optional<Person> personOptional = personRepository.findById(personId);
    if (!personOptional.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    person.setPersonId(personId);
    personRepository.save(person);
    return ResponseEntity.noContent().build();
  }

  /**
   * Deletes a person.
   *
   * @param personId the person id
   */
  @ApiOperation(value = "Deletes a person.", notes = "Deletes a person according to personId.")
  @DeleteMapping(value = "{personId}")
  public void deletePerson(@ApiParam(value = "Id for the person.", required = true)@PathVariable("personId") long personId) {
    personRepository.deleteById(personId);
  }

  /**
   * Gets all invitations per person.
   *
   * @param personId the person id
   * @return the all invitations per person
   */
  @ApiOperation(value = "Gets all new invitations for a person.", notes = "Retrieves all new invitation for a person according to personId.")
  @GetMapping(value = "{personId}/invitations", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Invitation> getAllInvitationsPerPerson(@ApiParam(value = "Id for the person.", required = true)@PathVariable("personId") long personId) {
//    return personRepository.findById(personId).get().getInvitations();

    Optional<Person> personOptional = personRepository.findById(personId);
    if (personOptional.isPresent()) {
      List<Invitation> tempInvites = invitationRepository.findAll();
      List<Invitation> toDeliver = new LinkedList<>();
      for (Invitation invitation :
          tempInvites) {
        if (invitation.getUserReceiverId() == personOptional.get().getPersonId()
        && !invitation.getWasDelivered()) {
          toDeliver.add(invitation);
          invitation.setWasDelivered(true);
        }
      }
      personOptional.get().setInvitations(tempInvites);
      personRepository.save(personOptional.get());
      return toDeliver;
    }
    return new LinkedList<>();
  }

  /**
   * Gets a single invitaiton.
   *
   * @param invitationId the invitation id
   * @return the invitation per person
   */
  @ApiOperation(value = "Gets a single invitation.", notes = "Retrieves a single invitation associated with a person, according to personId.")
  @GetMapping(value = "{personId}/invitations/{invitationId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Invitation getInvitationPerPerson(@ApiParam(value = "Id for the invitation.", required = true)@PathVariable("invitationId") long invitationId) {
    return invitationRepository.findById(invitationId).get();
  }

  /**
   * Add an invitation to a person.
   *
   * @param personId the person id
   * @param invitation the invitation
   * @return the response entity
   */
  @ApiOperation(value = "Adds an invitation to a person.", notes = "Adds an invitation to a person according to personId.")
  @PostMapping(value = "{personId}/invitations", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Invitation> addInvitation(@ApiParam(value = "Id for the person.", required = true)@PathVariable("personId") long personId,
      @ApiParam(value = "Invitation object.", required = true)@RequestBody Invitation invitation) {
    Optional<Person> personOptional = personRepository.findById(personId);
    if (!personOptional.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    List<Invitation> tempInvites = personOptional.get().getInvitations();
    tempInvites.add(invitation);
    personOptional.get().setInvitations(tempInvites);
    personRepository.save(personOptional.get());
    return ResponseEntity.created(invitation.getHref()).body(invitation);
  }

  /**
   * Update an invitation.
   *
   * @param invitationId the invitation id
   * @return the response entity
   */
  @ApiOperation(value = "Updates an invitation.", notes = "Updates a single person's id, based on personId and invitationId.")
  @PutMapping(value = "{personId}/invitations/{invitationId}")
  public ResponseEntity<Invitation> updateInvitation(@ApiParam(value = "Id for the invitation.", required = true)
      @PathVariable("invitationId") long invitationId) {
    Optional<Invitation> invitationOptional = invitationRepository.findById(invitationId);
    if (invitationOptional.isPresent()) {
      invitationOptional.get().setWillAttend(true);
      invitationRepository.save(invitationOptional.get());
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  /**
   * Gets people nearby.
   *
   * @param requesterUser the requester user
   * @param personId the person id
   * @return the people nearby
   */
  @ApiOperation(value = "Updates a person's location and gets the people near them.", notes = "Causes a person's location to be updated and retrieves a list of all the people that are near the new location.")
  @PutMapping(value = "{personId}/people", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Person> getPeopleNearby(@ApiParam(value = "Person object.", required = true)@RequestBody Person requesterUser,
      @ApiParam(value = "Id for the person.", required = true)@PathVariable("personId") long personId) {
    Optional<Person> personOptional = personRepository.findById(personId);
    if (personOptional.isPresent()) {
      updateUser(requesterUser, personId);
      List<Person> tempPersons = new LinkedList<>();
      List<Square> tempSquare = personOptional.get().getClosestVertex().getSquares();
      for (Square square :
          tempSquare) {
        tempPersons.addAll(square.getPeople());
      }
      return tempPersons;
    }
    return new LinkedList<>();
  }


  private void updateUser(Person requesterUser, long personId) {
    requesterUser.setPersonId(personId);
//    personRepository.save(requesterUser);
    updateCurrentSquare(requesterUser);
  }

  private void addToSquare(Person requesterUser) {
//    if (requesterUser.getCurrentSquareId() != 0) {
//      if(!squareRepository.findById(requesterUser.getCurrentSquareId()).get().getPeople().contains(requesterUser)){
//        squareRepository.findById(requesterUser.getCurrentSquareId()).get().getPeople()
//            .add(requesterUser);
//      }
//    }
  }

  private void removeFromSquare(Person requesterUser) {
    Square toRemove = requesterUser.getCurrentSquare();
    requesterUser.setCurrentSquare(null);
  }

  private void updateCurrentSquare(Person requesterUser) {
    int row = 0;
    int col = 0;
    int squareInt = 1;
    double latitude = requesterUser.getLatitude();
    double longitude = requesterUser.getLongitude();
    if (latitude < SOUTH_LAT_LINE) {
      row = 0;
    } else if (latitude < MIDDLE_LAT_LINE) {
      row = 1;
    } else if (latitude < NORTH_LAT_LINE) {
      row = 2;
    } else {
      row = 3;
    }
    if (longitude > EAST_LONG_LINE) {
      col = 4;
    } else if (longitude > MIDDLE_LONG_LINE) {
      col = 3;
    } else if (longitude > WEST_LONG_LINE) {
      col = 2;
    } else {
      col = 1;
    }
    squareInt = 1 * col + 4 * row;

    switch (squareInt) {
      case 1:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_1_ID).get());
        requesterUser
            .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_1_ID).get());
        break;
      case 2:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_2_ID).get());
        if (isLeftOfMid(requesterUser, requesterUser.getCurrentSquare())) {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_1_ID).get());
        } else {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_2_ID).get());
        }
        break;
      case 3:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_3_ID).get());
        if (isLeftOfMid(requesterUser, requesterUser.getCurrentSquare())) {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_2_ID).get());
        } else {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_3_ID).get());
        }
        break;
      case 4:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_4_ID).get());
        requesterUser
            .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_3_ID).get());
        break;
      case 5:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_5_ID).get());
        if (isAboveMid(requesterUser, requesterUser.getCurrentSquare())) {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_4_ID).get());
        } else {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_1_ID).get());
        }
        break;
      case 6:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_6_ID).get());
        if (isLeftOfMid(requesterUser, requesterUser.getCurrentSquare())) {
          if (isAboveMid(requesterUser, requesterUser.getCurrentSquare())) {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_4_ID).get());
          } else {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_1_ID).get());
          }
        } else {
          if (isAboveMid(requesterUser, requesterUser.getCurrentSquare())) {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_5_ID).get());
          } else {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_2_ID).get());
          }
        }
        break;
      case 7:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_7_ID).get());
        if (isLeftOfMid(requesterUser, requesterUser.getCurrentSquare())) {
          if (isAboveMid(requesterUser, requesterUser.getCurrentSquare())) {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_5_ID).get());
          } else {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_2_ID).get());
          }
        } else {
          if (isAboveMid(requesterUser, requesterUser.getCurrentSquare())) {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_6_ID).get());
          } else {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_3_ID).get());
          }
        }
        break;
      case 8:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_8_ID).get());
        if (isAboveMid(requesterUser, requesterUser.getCurrentSquare())) {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_6_ID).get());
        } else {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_3_ID).get());
        }
        break;
      case 9:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_9_ID).get());
        if (isAboveMid(requesterUser, requesterUser.getCurrentSquare())) {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_7_ID).get());
        } else {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_4_ID).get());
        }
        break;
      case 10:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_10_ID).get());
        if (isLeftOfMid(requesterUser, requesterUser.getCurrentSquare())) {
          if (isAboveMid(requesterUser, requesterUser.getCurrentSquare())) {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_7_ID).get());
          } else {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_4_ID).get());
          }
        } else {
          if (isAboveMid(requesterUser, requesterUser.getCurrentSquare())) {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_8_ID).get());
          } else {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_5_ID).get());
          }
        }
        break;
      case 11:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_11_ID).get());
        if (isLeftOfMid(requesterUser, requesterUser.getCurrentSquare())) {
          if (isAboveMid(requesterUser, requesterUser.getCurrentSquare())) {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_8_ID).get());
          } else {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_5_ID).get());
          }
        } else {
          if (isAboveMid(requesterUser, requesterUser.getCurrentSquare())) {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_9_ID).get());
          } else {
            requesterUser
                .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_6_ID).get());
          }
        }
        break;
      case 12:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_12_ID).get());
        if (isAboveMid(requesterUser, requesterUser.getCurrentSquare())) {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_9_ID).get());
        } else {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_6_ID).get());
        }
        break;
      case 13:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_13_ID).get());
        requesterUser
            .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_7_ID).get());
        break;
      case 14:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_14_ID).get());
        if (isLeftOfMid(requesterUser, requesterUser.getCurrentSquare())) {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_7_ID).get());
        } else {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_8_ID).get());
        }
        break;
      case 15:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_15_ID).get());
        if (isLeftOfMid(requesterUser, requesterUser.getCurrentSquare())) {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_8_ID).get());
        } else {
          requesterUser
              .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_9_ID).get());
        }
        break;
      case 16:
        requesterUser.setCurrentSquare(squareRepository.findById(SquareController.BOX_16_ID).get());
        requesterUser
            .setClosestVertex(vertexRepository.findById(VertexController.VERTEX_9_ID).get());
        break;
    }

    personRepository.save(requesterUser);
  }

  private boolean isLeftOfMid(Person person, Square square) {
    return person.getLongitude()
        < (square.getLongitudeUpperBound() - square.getLongitudeLowerBound()) / 2
        + square.getLongitudeLowerBound();
  }

  private boolean isAboveMid(Person person, Square square) {
    return person.getLatitude()
        > (square.getLatitudeUpperBound() - square.getLatitudeLowerBound()) / 2
        + square.getLatitudeLowerBound();
  }

}
