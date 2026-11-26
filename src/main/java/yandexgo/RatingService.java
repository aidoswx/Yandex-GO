package yandexgo;

public final class RatingService {
    public void rateRide(Ride ride, Passenger passenger, int stars) {
        if (ride.getPassenger() != passenger) {
            throw new IllegalArgumentException("Оценить поездку может только её пассажир.");
        }
        ride.rate(stars);
    }
}
