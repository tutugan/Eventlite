package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import uk.ac.man.cs.eventlite.dao.EventRepository;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	@Autowired
	private VenueService venueService;
	
	@Autowired 
	private EventService eventService;
	

	@ExceptionHandler(VenueNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String venueNotFoundHandler(VenueNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "venues/not_found";
	}

	@GetMapping("/{id}")
	public String getVenue(@PathVariable("id") long id, Model model) {
		model.addAttribute("venue", venueService.findById(id));
		if (venueService.findById(id).isPresent()) {
			venueService.findById(id).ifPresent(venue -> model.addAttribute(venue));
		}
		else {
			throw new VenueNotFoundException(id);
		}
		
		Venue venue = venueService.findById(id).get();
		Iterable<Event> events = eventService.findAllByVenue( venue);
		List<Event> upcomingEvents = new ArrayList<Event>();
		for (Event event : events) {
			if( event.getDate().compareTo(LocalDate.now()) > 0)
				upcomingEvents.add(event);
			else if((event.getDate().compareTo(LocalDate.now()) == 0) &&
					event.getTime().compareTo(LocalTime.now()) >= 0)
				upcomingEvents.add(event);
		
		}
	
		model.addAttribute("venueUpcomingEvents", upcomingEvents);

		return "venues/venue-information";
	}

	@GetMapping
	public String getAllVenues(Model model) {

		model.addAttribute("venues", venueService.findAll());

		return "venues/index";
	}
	
	
	@GetMapping(value = "/venue_create")
	public String createNewVenue(Model model) {
	
	model.addAttribute("venues", venueService.findAll());

	return "venues/venue_create";
}
	
	@PostMapping(value = "/venue_create")
	public String createNewVenue(@ModelAttribute Venue venue,Model model) {
		
		model.addAttribute("venue", venue);
		venueService.saveVenue(venue);
		
		return "venues/venue_create";
	}

	@GetMapping("/venues")
	public String search(Model model, String keyword){
		model.addAttribute("venues", venueService.searchVenues(keyword));
		return "venues/index";
	}
}