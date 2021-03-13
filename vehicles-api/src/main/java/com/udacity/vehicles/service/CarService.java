package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository carRepository;
    private final MapsClient mapsClient;
    private final PriceClient priceClient;

    public CarService(CarRepository carRepository, MapsClient mapsClient, PriceClient priceClient) {
        this.mapsClient = mapsClient;
        this.priceClient = priceClient;
        this.carRepository = carRepository;
    }

    /**
     * NOTE: I have Updated the code to return all Cars with Location and Price information
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        List<Car> cars = new ArrayList<>();
        for(Car car : carRepository.findAll()){
            // Assign Price and Location to the car
            car.setPrice(priceClient.getPrice(car.getId()));
            car.setLocation(mapsClient.getAddress(car.getLocation()));
            cars.add(car);
        }
        return cars;
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        Optional<Car> car = carRepository.findById(id);

        if (!car.isPresent())
            throw new CarNotFoundException("ERROR: Car with ID: " + id + " Not found");

        /**
         * Note: The car class file uses @transient, meaning you will need to call
         *   the pricing service each time to get the price.
         */
        String vehiclePrice = priceClient.getPrice(id);
        car.get().setPrice(vehiclePrice);

        /**
         * Note: The Location class file also uses @transient for the address,
         * meaning the Maps service needs to be called each time for the address.
         */
        Location location = mapsClient.getAddress(car.get().getLocation());
        car.get().setLocation(location);

        return car.get();
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the carRepository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return carRepository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return carRepository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return carRepository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        Optional<Car> car = carRepository.findById(id);
        if (car.isPresent()) {
            carRepository.deleteById(id);
        } else {
            throw new CarNotFoundException("ERROR: Car with ID: ".concat(id.toString()).concat(" Not found"));
        }
    }
}
