package vessels;

import java.util.Locale;

/**
 * Represents a single type of vessel. Different vessel types have different innate properties, and are designed for
 * varying purposes.
 */
public enum VesselType {
    /**
     * A Base is a vessel or collection of vessels designed to remain stationary in a specific celestial body's surface
     * to gather scientific data, house kerbals for an indefinite period of time, serve as a pit-stop for other vessels,
     * or any combination of the previous objectives.
     */
    BASE,
    /**
     * A Station is a vessel or collection of vessels designed to remain stationary in a specific celestial body's orbit
     * to gather scientific data, house kerbals for an indefinite period of time, serve as a pit-stop for other vessels,
     * or any combination of the previous objectives.
     */
    STATION,
    /**
     * A Ship is a general purpose vessel used to transport kerbals, resources, scientific experiments or parts (or any
     * combination of the aforementioned) to a different location, and perhaps back from said location. Ships usually
     * require a Kerbal to pilot it.
     */
    SHIP,
    /**
     * A Plane is a specific type of {@link VesselType#SHIP}/{@link VesselType#PROBE}, designed to use control surface
     * to provide lift in atmospheric environments to achieve its purposes. Planes usually have the ability to land in
     * a celestial body.
     */
    PLANE,
    /**
     * A Lander is a further specification of {@link VesselType#SHIP}/{@link VesselType#PROBE}, designed to be able to
     * land in a specific celestial body (or more), usually with the ability to return to orbit after landing.
     */
    LANDER,
    /**
     * A Rover is a specialized vessel designed to use wheels as main propulsion to navigate around a celestial body's
     * surface. Rovers may include other propulsion devices to achieve purposes other than navigation.
     */
    ROVER,
    /**
     * Relays are a specific type of {@link VesselType#PROBE} designed to serve as a connection source for vessels in
     * range. Relays are usually deployed around a celestial body to allow for long range communications with the KSC.
     */
    RELAY,
    /**
     * Probes are a variant of the {@link VesselType#SHIP} type, with kerbal-less flight capabilities. Without a strong
     * enough antenna to connect directly to the KSC, probes require connection with nearby {@link VesselType#RELAY}
     * vessels.
     */
    PROBE,
    /**
     * Boats are a specialized vessel designed to travel mainly across liquid environments found on the surface of
     * celestial bodies such as Kerbin or Eve. Ships may include other propulsion devices to achieve purposes other
     * than navigation.
     */
    BOAT,
    /**
     * Debris are remains of other vessel types either destroyed by collisions, discarded as stages, or marked as debris
     * by the player. They usually have no means of control, even with kerbals around.
     */
    DEBRIS,
    /**
     * Other types of vessels match with this category.
     */
    OTHER,
    /**
     * Unknown vessels represent the vessel type of a {@link Concept} deleted by the user.
     */
    UNKNOWN;

    @Override
    public String toString() {
        return name().substring(0, 1).toUpperCase(Locale.ROOT) + name().substring(1).toLowerCase(Locale.ROOT);
    }
}
