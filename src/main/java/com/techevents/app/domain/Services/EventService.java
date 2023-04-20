package com.techevents.app.domain.Services;

import com.techevents.app.domain.Dtos.EventRequest;
import com.techevents.app.domain.Models.Event;
import com.techevents.app.Repositories.ICategoryRepository;
import com.techevents.app.Repositories.IEventRepository;
import com.techevents.app.security.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EventService {
    private final IEventRepository eventRepository;
    private final AdminService adminService;
    private final ICategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AuthService authService;



    public EventService(IEventRepository eventRepository, AdminService adminService, ICategoryRepository categoryRepository, UserRepository userRepository, AuthService authService) {
        this.eventRepository = eventRepository;
        this.adminService = adminService;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.authService = authService;
    }
    public List<Event> findAll(){
        var auth = authService.getAuthUser();
        var eventList = this.eventRepository.findAll();

        eventList.forEach(event -> {
            event.alreadyRegistered(auth);
        });
        return eventList;
    }

    public List<Event> findByName(String name){
        return eventRepository.findEventByName(name);
    }

    public List<Event> findHighlightByName(String name){
        return eventRepository.findHighlightEventByName(name);
    }

    public Event findById(Long id) {
        var eventOptional = this.eventRepository.findById(id);
        if(eventOptional.isEmpty()) throw new RuntimeException("The event with ID " + id + " was not found in our database. Please double-check the ID and try again with a valid one.");
        return eventOptional.get();
    }

    public List<Event> findAllHighLights(){
        var events = eventRepository.findByHighlightsTrue();
        return events;
    }

    public List<Event> findAvailableEvents(){
        var events = eventRepository.findAll();
        var availableEvents = events.stream().filter(event -> event.isAvailable()).toList();

        return availableEvents;
    }
    public List<Event> findNotAvailableEvents(){
        var events = eventRepository.findAll();
        var notAvailableEvents = events.stream().filter(event -> !event.isAvailable()).toList();

        return notAvailableEvents;
    }

    public List<Event> findEventsByCategory(@PathVariable Long id){
        return eventRepository.findByCategory_Id(id);
    }

    public static boolean validateURL(String urlString){
        try{
            new URL(urlString).toURI();
        }
        catch (Exception e){
            System.out.println(e);
            return false;
        }
        return true;
    }

    public Event addEvent(EventRequest request) {
        var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("We're sorry, the selected category doesn't exist. Please try again with a valid category ID."));

        LocalDate eventDate = LocalDate.parse(request.getEventDate(),DateTimeFormatter.ISO_LOCAL_DATE);
        LocalTime eventHour = LocalTime.parse(request.getEventHour().trim(),DateTimeFormatter.ofPattern("HH:mm"));

        if (eventDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("We're sorry, but to create a new event the date must be scheduled for a future date. Please select a date that is after today and try again");
        }

        var event = new Event();
        var urlDefault = "https://static.thenounproject.com/png/1554489-200.png";

        event.setName(request.getName());
        event.setOrganizer(request.getOrganizer());
        event.setDescription(request.getDescription());
        event.setHighlights(request.getHighlights());
        event.setLocation(request.getLocation());
        event.setCategory(category);

        event.setImg(request.getImg());
        if(validateURL(request.getImg()) == false){
            event.setImg(urlDefault);
        }

        event.setEventDate(eventDate);
        event.setEventHour(eventHour);

        return this.eventRepository.save(event);
    }

    public void deleteById(Long eventId) {
        var event = eventRepository.findById(eventId).get();
        this.eventRepository.deleteById(eventId);
    }

    public void editById(Long eventId, EventRequest newEvent){
        var event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("The event you are looking for cannot be found. Please try with a different ID or contact our support team for assistance."));
        var category = categoryRepository.findById(newEvent.getCategoryId()).orElseThrow(() -> new RuntimeException("The requested category was not found. Please try again with a valid category."));

        LocalTime eventHour = LocalTime.parse(newEvent.getEventHour(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalDate eventDate = LocalDate.parse(newEvent.getEventDate(), DateTimeFormatter.ISO_LOCAL_DATE);

        event.setName(newEvent.getName());
        event.setOrganizer(newEvent.getOrganizer());
        event.setDescription(newEvent.getDescription());
        event.setHighlights(newEvent.getHighlights());
        event.setLocation(newEvent.getLocation());
        event.setEventHour(eventHour);
        event.setEventDate(eventDate);
        event.setCategory(category);
        event.setImg(newEvent.getImg());
        eventRepository.save(event);
    }
}
