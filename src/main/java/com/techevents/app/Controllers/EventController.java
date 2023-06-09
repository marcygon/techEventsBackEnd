package com.techevents.app.Controllers;


import com.techevents.app.Repositories.ICategoryRepository;
import com.techevents.app.domain.Dtos.EventRequest;
import com.techevents.app.domain.Models.Event;
import com.techevents.app.domain.Services.EventService;

import com.techevents.app.domain.Services.JoinAnEventService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final ICategoryRepository categoryRepository;
    private final JoinAnEventService registerService;

    public EventController(EventService eventService, ICategoryRepository categoryRepository, JoinAnEventService registerService) {
        this.eventService = eventService;
        this.categoryRepository = categoryRepository;
        this.registerService = registerService;
    }


    @GetMapping
    public ResponseEntity<List<Event>> getAll(@RequestParam(name = "name", required = false) String name) {
        List<Event> eventsList = (name != null) ? eventService.filterAllEventsByName(name) : eventService.findAll();
        return ResponseEntity.ok(eventsList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getById(@PathVariable Long id) {
        return ResponseEntity.ok(this.eventService.findById(id));
    }

    @GetMapping("/highlights")
    public ResponseEntity<List<Event>> findAllHighLights(@RequestParam(name = "name", required = false) String name){
        List<Event> highlightEventsList = (name != null) ? eventService.filterHighlightByName(name) : eventService.findAllHighLights();
        return ResponseEntity.ok(highlightEventsList);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Event>> findAvailable(@RequestParam(name = "name", required = false) String name){
        List<Event> availableEventsList = (name != null) ? eventService.filterAvailableEventsByName(name) : eventService.findAvailableEvents();
        return ResponseEntity.ok(availableEventsList);
    }

    @GetMapping("/notAvailable")
    public ResponseEntity<List<Event>> findNotAvailable(@RequestParam(name = "name", required = false) String name){
        List<Event> notAvailableEventsList = (name != null) ? eventService.filterNotAvailableEventsByName(name) : eventService.findNotAvailableEvents();
        return ResponseEntity.ok(notAvailableEventsList);
    }

    @GetMapping("category/{id}")
    public ResponseEntity<List<Event>> findEventsByCategory(@PathVariable Long id){
        return ResponseEntity.ok(this.eventService.findEventsByCategory(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Event> addEvent(@RequestBody EventRequest request){
        return ResponseEntity.ok(this.eventService.addEvent(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteById(@PathVariable Long id){
        this.eventService.deleteById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void editById(@PathVariable Long id, @RequestBody EventRequest changes){
        this.eventService.editById(id, changes);
    }

    @PostMapping("/{eventId}/joinEvent")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity joinAnEvent(@PathVariable Long eventId){
        registerService.loggedUserRegisterToEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/joined")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<Event>> joinedEvents(){
        List<Event> eventsJoined = registerService.loggedUserCheckEventsJoined();
        return ResponseEntity.ok(eventsJoined);
    }
}
