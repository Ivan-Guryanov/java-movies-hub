package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;

public class MoviesStore {
    private LinkedHashMap<Integer, Movie> listOfFilms = new LinkedHashMap<>();
    private int counter = 0;

    public List<Movie> getAllMovies() {
        return new ArrayList<>(listOfFilms.values());
    }

    public void removeAll() {
        listOfFilms.clear();
    }

    public void newFilm(String title, int year) {
        counter++;
        listOfFilms.put(counter, new Movie(counter, title, year));
    }

    public Movie getLastMovie() {
        return listOfFilms.sequencedValues().getLast();
    }

    public LinkedHashMap<Integer, Movie> getListOfFilms() {
        return listOfFilms;
    }

    public void setListOfFilms(LinkedHashMap<Integer, Movie> listOfFilms) {
        this.listOfFilms = listOfFilms;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void deleteMovie(int id) {
        listOfFilms.remove(id);
    }
}