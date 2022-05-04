package uk.ac.man.cs.eventlite.controllers;


import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
//import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import twitter4j.Status;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "events/not_found";
	}

	@GetMapping("/{id}")
	public String getEvent(@PathVariable("id") long id, Model model) {
		throw new EventNotFoundException(id);
	}
	
	@GetMapping
	public String getAllEvents(Model model) {
		LocalDate date = LocalDate.now( ZoneId.of( "Europe/London" ) ) ;
		model.addAttribute("events", eventService.findAll());
		model.addAttribute("upcommingEvents", eventService.findAllByDateAfter(date));
		model.addAttribute("previousEvents", eventService.findAllByDateBefore(date));
		
		Paging p = new Paging();
	    p.setCount(5);
	    
	    try {
	    	ResponseList<Status> tweets = TwitterService().getUserTimeline(p);
			model.addAttribute("tweets", tweets);
			}
			catch(TwitterException e) {
				System.out.println("Twitter Error");
			}

		return "events/index";
	}
	
	@GetMapping(value = "/event_create")
	public String createNewEvent(Model model) {
	
	model.addAttribute("events", eventService.findAll());
	model.addAttribute("venues", venueService.findAll());

	return "events/event_create";
}

	@DeleteMapping("/{id}")
	public String deleteEvent(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		if (!eventService.existsById(id)) {
			throw new EventNotFoundException(id);
		}
		eventService.deleteById(id);
		redirectAttrs.addFlashAttribute("ok_message", "Event deleted.");
		return "events/index";
	}

		
	
	
	
	@PostMapping(value = "/event_create")
	public String createNewEvent(@ModelAttribute Event event,Model model) {
		
		
		
		//Venue eventVenue = venueService.findById(venue);
		//event.setVenue(eventVenue);
		
		
		
		model.addAttribute("event", event);
		eventService.saveEvent(event);
		//eventService.deleteAll();
		
		return "events/event_create";
	}
	
	
	
	
	

	@GetMapping("/events")
	public String search(Model model, String keyword){
		LocalDate date = LocalDate.now( ZoneId.of( "Europe/Paris" ) ) ;
		model.addAttribute("upcommingEvents", eventService.searchAfter(date, keyword));
		model.addAttribute("previousEvents", eventService.searchBefore(date, keyword));
		return "events/index";
	}
	
	public Twitter TwitterService() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey("gkjSk5uYLm3G3cd6HsEFNpxU0")
		.setOAuthConsumerSecret("6d4gDpSqZxY1frP6so6Y4bfmkG6bR5dPaNIL12u3tHfKF8kpEh")
		.setOAuthAccessToken("839175127019442179-2O7HI9m1QJICRYo1yZ7hwQhu78FA6HN")
		.setOAuthAccessTokenSecret("ublJ3DP4EyYQTM7SR6bIJzT8FTIFCGI3noNXCbivCm1Pp");
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		return twitter;
	}
	
	
	
	
}
