package vessels;

/**
 * Describes additional properties that further describe the {@link Concept} they're applied to.
 */
public enum VesselProperty {
    /**
     * Indicates that the vessel is VTOL capable. In other words, the vessel is able to take off vertically, and land
     * in a similar fashion. This property usually applies to spacecraft.
     */
    VTOL,
    /**
     * Indicates that the vessel uses SSRT, or Single Stage Rocket Technology. In summary, it means that the vessel is
     * able to achieve orbit from a standstill using a single stage, usually from Kerbin.
     */
    SSRT,
    /**
     * Indicates that the vessel is powered in its entirety by electricity, or has a stage that works in that fashion.
     */
    ELECTRIC,
    /**
     * Indicates that the vessel is powered by ion propulsion, has a stage with dedicated ion thrusters, or a
     * combination of the two.
     */
    ION_POWERED,
    /**
     * Indicates that the vessel cannot carry crew members.
     */
    NO_CREW_SPACE,
    /**
     * Indicates that, in addition to the vessel's type, it can perform as a {@link VesselType#BOAT}. This means that
     * the vessel can use its propulsion systems to navigate through a celestial body's liquid areas.
     */
    BOAT_CAPABLE,
    /**
     * Indicates that the vessel can land on liquids.
     */
    WATER_LANDABLE,
    /**
     * Indicates that the vessel has enough parachutes to safely descend through an atmosphere and land with them.
     * This condition doesn't apply to Duna, meaning that parachuted craft might require additional impulse to
     * safely touch down.
     */
    HAS_PARACHUTES,
    /**
     * Indicates that the vessel has one or more cargo storage modules, with which the craft can carry additional parts.
     * Crew capsules' cargo storage slots don't qualify as a requirement for this property.
     */
    CARGO_SPACE
}
