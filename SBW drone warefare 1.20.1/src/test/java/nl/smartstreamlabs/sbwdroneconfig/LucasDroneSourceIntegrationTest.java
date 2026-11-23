package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LucasDroneSourceIntegrationTest {
    @Test
    void lucasDroneEntityKeepsTheExpectedCrouchPickupBehavior() throws Exception {
        String source = readJavaSource("LucasDroneEntity.java");

        assertTrue(source.contains("player.isCrouching()"),
                "The LUCAS drone should mirror the crouch pickup interaction.");
        assertTrue(source.contains("ModTags.Items.TOOLS_CROWBAR"),
                "The LUCAS drone pickup interaction should keep the same empty-hand or crowbar requirement.");
        assertTrue(source.contains("new ItemStack(AddonItems.LUCAS_DRONE.get())"),
                "Picking up the LUCAS drone should return the LUCAS drone item.");
    }

    @Test
    void lucasDroneEntityUsesTheDedicatedFixedWingTravelController() throws Exception {
        String source = readJavaSource("LucasDroneEntity.java");

        assertTrue(source.contains("public void travel()"),
                "The LUCAS drone should override travel so it can stop using the quadcopter movement branch.");
        assertTrue(source.contains("LucasFuelSystem.beforeLucasTravel(this);"),
                "The LUCAS drone travel override should use the dedicated gasoline fuel hook before movement.");
        assertTrue(!source.contains("DroneBatterySystem.beforeDroneTravel(this);"),
                "The LUCAS drone should not use the generic electric battery travel hook.");
        assertTrue(source.contains("DroneJammerSystem.beforeDroneTravel(this);"),
                "The LUCAS drone travel override should keep the jammer hook before movement.");
        assertTrue(source.contains("DroneWeatherEffects.applyStormEffects(this);"),
                "The LUCAS drone travel override should keep the weather instability hook before movement.");
        assertTrue(source.contains("FiberOpticLinkSystem.beforeDroneTravel(this);"),
                "The LUCAS drone travel override should keep the fiber optic pre-travel hook.");
        assertTrue(source.contains("LucasFixedWingFlightController.travel(this);"),
                "The LUCAS drone travel override should delegate to the dedicated fixed-wing controller.");
        assertTrue(source.contains("FiberOpticLinkSystem.afterDroneTravel(this);"),
                "The LUCAS drone travel override should keep the fiber optic post-travel hook.");
    }

    @Test
    void lucasDroneUsesServerAuthoritativeGasolineFuel() throws Exception {
        String entitySource = readJavaSource("LucasDroneEntity.java");
        String fuelSource = readJavaSource("LucasFuelSystem.java");
        String controllerSource = readJavaSource("LucasFixedWingFlightController.java");
        String networkSource = readJavaSource("AddonNetwork.java");
        String mainSource = readJavaSource("SbwDroneRangeConfig.java");

        assertTrue(entitySource.contains("LUCAS_FUEL"),
                "The LUCAS drone should sync its own fuel value through entity data.");
        assertTrue(entitySource.contains("TAG_LUCAS_FUEL"),
                "The LUCAS drone should persist its fuel amount in NBT.");
        assertTrue(entitySource.contains("LucasFuelSystem.refuel(this, player, hand)"),
                "Right-click refueling should be handled before the normal drone interaction path.");
        assertTrue(entitySource.contains("getLucasFuelPercent()"),
                "The LUCAS drone should expose a fuel percentage for HUD and logic consumers.");

        assertTrue(fuelSource.contains("AddonConfig.gasolineJerrycanFuelAmount()"),
                "A gasoline jerrycan should use the configurable fuel amount.");
        assertTrue(readJavaSource("AddonConfig.java").contains(".defineInRange(\"gasolineJerrycanFuelAmount\", 1000"),
                "A gasoline jerrycan should default to 1000 fuel.");
        assertTrue(fuelSource.contains("drone.onGround()"),
                "Refueling should only be allowed while the LUCAS drone is on the ground.");
        assertTrue(fuelSource.contains("new ItemStack(AddonItems.EMPTY_JERRYCAN.get())"),
                "Using a gasoline jerrycan should produce an empty jerrycan.");
        assertTrue(fuelSource.contains("message.sbwdroneconfig.lucas_refueled"),
                "Successful refueling should show a clear player message.");
        assertTrue(fuelSource.contains("message.sbwdroneconfig.lucas_fuel_empty"),
                "Empty fuel should show a clear warning and stop powered flight.");
        assertTrue(fuelSource.contains("AddonNetwork.sendLucasFuel"),
                "Fuel changes should be synchronized to the controlling client.");

        assertTrue(controllerSource.contains("LucasFuelSystem.canUsePoweredFlight(drone)"),
                "The fixed-wing controller should gate powered takeoff/climb/throttle on LUCAS fuel.");
        assertTrue(controllerSource.contains("LucasFuelSystem.isOutOfFuel(drone)"),
                "The fixed-wing controller should let fuel-empty LUCAS drones glide down instead of powering through.");
        assertTrue(networkSource.contains("LucasFuelSyncMessage.class"),
                "The addon network channel should register the LUCAS fuel sync packet.");
        assertTrue(mainSource.contains("LucasFuelClient.init(modBus);"),
                "Client setup should register the LUCAS fuel HUD.");
    }

    @Test
    void lucasGasolineJerrycansAreSeparateCraftableItems() throws Exception {
        String itemsSource = readJavaSource("AddonItems.java");
        String creativeTabsSource = readJavaSource("AddonCreativeTabs.java");
        String langSource = readProjectFile("src", "main", "resources", "assets", "sbwdroneconfig", "lang", "en_us.json");

        assertTrue(itemsSource.contains("EMPTY_JERRYCAN"),
                "The addon should register an empty jerrycan item.");
        assertTrue(itemsSource.contains("GASOLINE_JERRYCAN"),
                "The addon should register a gasoline jerrycan item.");
        assertTrue(creativeTabsSource.contains("output.accept(AddonItems.EMPTY_JERRYCAN.get());"),
                "The empty jerrycan should be visible in the addon creative tab.");
        assertTrue(creativeTabsSource.contains("output.accept(AddonItems.GASOLINE_JERRYCAN.get());"),
                "The gasoline jerrycan should be visible in the addon creative tab.");
        assertTrue(langSource.contains("\"item.sbwdroneconfig.empty_jerrycan\""),
                "The language file should name the empty jerrycan item.");
        assertTrue(langSource.contains("\"item.sbwdroneconfig.gasoline_jerrycan\""),
                "The language file should name the gasoline jerrycan item.");
        assertTrue(Files.exists(Path.of("src", "main", "resources", "data", "sbwdroneconfig", "recipes", "empty_jerrycan.json")),
                "The empty jerrycan should have a normal crafting recipe.");
        assertTrue(Files.exists(Path.of("src", "main", "resources", "data", "sbwdroneconfig", "recipes", "gasoline_jerrycan.json")),
                "The gasoline jerrycan should have a normal crafting recipe.");
        assertTrue(Files.exists(Path.of("src", "main", "resources", "data", "sbwdroneconfig", "recipes", "gasoline_jerrycan_vehicle_assembling.json")),
                "The gasoline jerrycan should also be craftable in the Vehicle Assembling Table miscellaneous category.");
        assertTrue(Files.exists(Path.of("src", "main", "resources", "data", "sbwdroneconfig", "recipes", "empty_jerrycan_vehicle_assembling.json")),
                "The empty jerrycan should also be craftable in the Vehicle Assembling Table so the gasoline recipe chain is not blocked.");
    }

    @Test
    void lucasFuelConfigAndHudAreExposed() throws Exception {
        String configSource = readJavaSource("AddonConfig.java");
        String draftSource = readJavaSource("DroneConfigDraft.java");
        String screenSource = readJavaSource("DroneConfigScreen.java");
        String clientSource = readJavaSource("LucasFuelClient.java");
        String langSource = readProjectFile("src", "main", "resources", "assets", "sbwdroneconfig", "lang", "en_us.json");

        assertTrue(configSource.contains("LUCAS_MAX_FUEL_VALUE"),
                "The config should expose the LUCAS maximum fuel tank size.");
        assertTrue(configSource.contains("defineInRange(\"lucasMaxFuel\", 1000"),
                "The LUCAS max fuel default should be 1000.");
        assertTrue(configSource.contains("defineInRange(\"lucasFuelDrainIdle\", 0.02D"),
                "The LUCAS idle fuel drain default should be 0.02.");
        assertTrue(configSource.contains("defineInRange(\"lucasFuelDrainFlying\", 0.08D"),
                "The LUCAS flying fuel drain default should be 0.08.");
        assertTrue(configSource.contains("defineInRange(\"lucasFuelDrainClimb\", 0.14D"),
                "The LUCAS climb fuel drain default should be 0.14.");
        assertTrue(configSource.contains("defineInRange(\"lucasLowFuelWarningPercent\", 15"),
                "The LUCAS low fuel warning should default to 15 percent.");
        assertTrue(configSource.contains("define(\"lucasCanFlyWithoutFuel\", false)"),
                "The LUCAS drone should not be allowed to fly without fuel by default.");
        assertTrue(draftSource.contains("lucasMaxFuel"),
                "The config draft should include LUCAS fuel settings.");
        assertTrue(screenSource.contains("screen.sbwdroneconfig.config.label.lucas_max_fuel"),
                "The config GUI should expose the LUCAS fuel tank slider.");
        assertTrue(clientSource.contains("overlay.sbwdroneconfig.lucas_fuel"),
                "The LUCAS fuel HUD should render a fuel percentage line.");
        assertTrue(clientSource.contains("overlay.sbwdroneconfig.lucas_fuel_empty"),
                "The LUCAS fuel HUD should render the empty-fuel warning.");
        assertTrue(langSource.contains("\"screen.sbwdroneconfig.config.label.lucas_max_fuel\""),
                "The language file should include the LUCAS fuel config labels.");
        assertTrue(langSource.contains("\"overlay.sbwdroneconfig.lucas_fuel\""),
                "The language file should include the LUCAS fuel HUD label.");
    }

    @Test
    void lucasFixedWingControllerUsesArcadeThrottleAndClimbBindings() throws Exception {
        String source = readJavaSource("LucasFixedWingFlightController.java");

        assertTrue(source.contains("boolean throttleUp = drone.forwardInputDown();"),
                "The LUCAS controller should map W to throttle-up for the arcade fixed-wing controls.");
        assertTrue(source.contains("boolean throttleDown = drone.backInputDown();"),
                "The LUCAS controller should map S to throttle-down for the arcade fixed-wing controls.");
        assertTrue(source.contains("boolean braking = activelyControlled && isBrakeInput(drone);"),
                "Holding S without W should enter a dedicated LUCAS brake mode.");
        assertTrue(source.contains("activelyControlled && poweredFlight && !braking"),
                "Holding S should abort takeoff assist instead of keeping the throttle floor active.");
        assertTrue(source.contains("double targetSpeed = braking ? 0.0D : resolveTargetSpeed(throttle, minSpeed, maxSpeed);"),
                "Holding S should force the target speed to zero so the drone can actually stop.");
        assertTrue(source.contains("LucasFlightPhysicsMath.computeBrakeDrag(braking, drone.onGround())"),
                "Holding S should apply an extra horizontal brake drag after normal flight drag.");
        assertTrue(source.contains("if (drone.upInputDown())"),
                "The LUCAS controller should reserve Space for assisted climbing instead of throttle.");
        assertTrue(source.contains("if (drone.downInputDown())"),
                "The LUCAS controller should reserve Shift for assisted descending instead of throttle.");
        assertTrue(source.contains("drone.beginLucasTakeoffAssist()"),
                "The LUCAS controller should trigger the assisted takeoff window when a new control session starts.");
        assertTrue(source.contains("Math.max(AddonConfig.lucasMaxSpeed(), 3.20D)"),
                "The LUCAS controller should enforce a faster effective top-speed floor for old configs.");
        assertTrue(source.contains("Math.max(AddonConfig.lucasMinSpeed(), 0.85D)"),
                "The LUCAS controller should enforce a faster effective cruise floor for old configs.");
        assertTrue(source.contains("Math.max(AddonConfig.lucasTakeoffForwardBoost(), 1.15D)"),
                "The LUCAS controller should enforce a stronger launch-speed floor for old configs.");
        assertTrue(source.contains("Math.max(AddonConfig.lucasAcceleration(), 0.085D)"),
                "The LUCAS controller should enforce stronger acceleration for old configs.");
        assertTrue(source.contains("DESCEND_DIVE_MULTIPLIER"),
                "The LUCAS controller should expose a dedicated dive multiplier so Shift can force a real dive.");
        assertTrue(source.contains("MIN_DIVE_DESCENT_SPEED"),
                "The LUCAS controller should enforce a minimum dive speed floor for Shift input.");
        assertTrue(source.contains("Math.max(AddonConfig.lucasDescendSpeed() * DESCEND_DIVE_MULTIPLIER, MIN_DIVE_DESCENT_SPEED)"),
                "Holding Shift should trigger a much stronger dive command than the normal descend config alone.");
    }

    @Test
    void lucasDroneItemUsesTheLucasEntityRegistrationForDeployment() throws Exception {
        String source = readJavaSource("LucasDroneItem.java");

        assertTrue(source.contains("LucasPlatformBlock.interact")&&!source.contains("AddonEntities.LUCAS_DRONE.get().spawn"),
                "The LUCAS item must delegate exclusively to its launch platform.");
        assertTrue(source.contains("public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced)"),
                "The LUCAS drone item should override appendHoverText for its dedicated tooltip lines.");
        assertTrue(source.contains("tooltipComponents.add(Component.translatable(\"tooltip.sbwdroneconfig.lucas_drone.1\").withStyle(ChatFormatting.GRAY));"),
                "The first LUCAS drone tooltip line should be added to the item hover text in gray.");
        assertTrue(source.contains("tooltipComponents.add(Component.translatable(\"tooltip.sbwdroneconfig.lucas_drone.2\").withStyle(ChatFormatting.GRAY));"),
                "The second LUCAS drone tooltip line should be added to the item hover text in gray.");
    }

    @Test
    void fpvAndLucasDroneItemsExposeClearOperationalTooltips() throws Exception {
        String fpvSource = readJavaSource("CubedFpvDroneItem.java");
        String lucasSource = readJavaSource("LucasDroneItem.java");
        String langSource = readProjectFile("src", "main", "resources", "assets", "sbwdroneconfig", "lang", "en_us.json");

        assertTrue(fpvSource.contains("public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced)"),
                "The FPV drone item should expose a dedicated hover tooltip override.");
        assertTrue(fpvSource.contains("tooltip.sbwdroneconfig.cubed_fpv_drone.1"),
                "The FPV drone item should include a monitor control tooltip line.");
        assertTrue(fpvSource.contains("tooltip.sbwdroneconfig.cubed_fpv_drone.4"),
                "The FPV drone item should include a module and upgrade guidance line.");
        assertTrue(lucasSource.contains("tooltip.sbwdroneconfig.lucas_drone.3"),
                "The LUCAS drone item should describe its fixed-wing controls in the tooltip.");
        assertTrue(lucasSource.contains("tooltip.sbwdroneconfig.lucas_drone.4"),
                "The LUCAS drone item should include a drone inventory guidance line.");
        assertTrue(langSource.contains("\"tooltip.sbwdroneconfig.cubed_fpv_drone.1\""),
                "The language file should include the first FPV drone tooltip translation.");
        assertTrue(langSource.contains("\"tooltip.sbwdroneconfig.cubed_fpv_drone.4\""),
                "The language file should include the fourth FPV drone tooltip translation.");
        assertTrue(langSource.contains("\"tooltip.sbwdroneconfig.lucas_drone.4\""),
                "The language file should include the fourth LUCAS drone tooltip translation.");
    }

    @Test
    void lucasDroneModelTargetsItsDedicatedRearPropellerAnimationBone() throws Exception {
        String source = readJavaSource("LucasDroneModel.java");

        assertTrue(source.contains("private static final String[] PROPELLER_BONES = {\"propeller\"};"),
                "The LUCAS drone should animate only the dedicated propeller bone so the nose mesh does not spin.");
        assertTrue(source.contains("bone.setRotZ(angle * PROPELLER_DIRECTIONS[index]);"),
                "The LUCAS drone propeller animation should spin around the pusher prop axis with a Z rotation.");
    }

    @Test
    void lucasDroneAudioUsesDedicatedAssetsAndLoopingClientSoundManager() throws Exception {
        String addOnSoundsSource = readJavaSource("AddonSounds.java");
        String soundsSource = readProjectFile("src", "main", "resources", "assets", "sbwdroneconfig", "sounds.json");
        String instanceSource = readJavaSource("LucasDroneEngineSoundInstance.java");
        String managerSource = readJavaSource("LucasDroneSoundManager.java");
        String mainSource = readJavaSource("SbwDroneRangeConfig.java");
        String genericSource = readJavaSource("DroneEngineSoundClient.java");
        String commandSource = readJavaSource("FiberOpticDebugCommand.java");

        assertTrue(addOnSoundsSource.contains("LUCAS_DRONE_ENGINE = SOUND_EVENTS.register("),
                "The addon should explicitly register the primary LUCAS engine sound event.");
        assertTrue(addOnSoundsSource.contains("LUCAS_DRONE_ENGINE_FAR = SOUND_EVENTS.register("),
                "The addon should expose the optional far LUCAS engine sound event registration.");
        assertTrue(soundsSource.contains("\"lucas_drone_engine\""),
                "sounds.json should define the primary LUCAS engine sound key.");
        assertTrue(soundsSource.contains("\"name\": \"sbwdroneconfig:lucas_drone_engine\""),
                "sounds.json should point the primary LUCAS engine sound at the normalized lowercase root sound path.");
        assertTrue(soundsSource.contains("\"lucas_drone_engine_far\""),
                "sounds.json should define the optional far LUCAS engine sound entry.");
        assertTrue(Files.exists(Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "sounds", "lucas_drone_engine.ogg")),
                "The primary LUCAS engine .ogg should exist directly under assets/sbwdroneconfig/sounds/ with a lowercase filename.");

        String externalInstanceSource = readJavaSource("DroneExternalSoundInstance.java");
        String externalManagerSource = readJavaSource("DroneAudioManager.java");
        String externalProfileSource = readJavaSource("DroneAudioProfile.java");

        assertTrue(externalInstanceSource.contains("extends AbstractTickableSoundInstance"),
                "The LUCAS drone should use managed client looping tickable sound instances.");
        assertTrue(externalInstanceSource.contains("this.looping = true;"),
                "The managed LUCAS sound layers should loop continuously.");
        assertTrue(externalInstanceSource.contains("this.relative = false;"),
                "The managed LUCAS sound layers should stay positional instead of relative to the player.");
        assertTrue(externalInstanceSource.contains("this.attenuation = Attenuation.NONE;"),
                "The managed LUCAS layers should use custom long-range attenuation instead of the old short default cutoff.");
        assertTrue(externalManagerSource.contains("lucasDrone.getLucasThrottle()"),
                "The managed LUCAS audio should derive its mix from the drone throttle instead of the generic drone monitor penalty.");
        assertTrue(externalManagerSource.contains("lucasDrone.getLucasAirspeed()"),
                "The managed LUCAS audio should derive its mix from the fixed-wing airspeed state.");
        assertTrue(externalProfileSource.contains("AddonConfig.lucasExternalVolume()"),
                "The managed LUCAS audio should honor the configurable external LUCAS master volume.");

        assertTrue(externalManagerSource.contains("private static final Map<UUID, ManagedDroneAudio> ACTIVE_AUDIO"),
                "The external audio manager should track one managed sound set per drone UUID.");
        assertTrue(externalManagerSource.contains("minecraft.getSoundManager().play(sound);"),
                "The external audio manager should start managed looping layers through the client sound manager.");
        assertTrue(externalManagerSource.contains("new LucasExternalSoundInstance(this.drone, layer)"),
                "The external audio manager should spawn the layered LUCAS external sound instance.");
        assertTrue(mainSource.contains("DroneAudioManager.init(modBus);"),
                "Client setup should register the unified drone audio manager.");

        assertTrue(externalManagerSource.contains("if (isManagedDroneAudio(vehicle))"),
                "The generic vehicle engine hook should skip Cubed FPV and LUCAS so only the unified manager owns their loops.");
        assertTrue(commandSource.contains(".then(Commands.literal(\"playsoundtest\")"),
                "The sbwdrone command tree should expose a playsoundtest command for the LUCAS engine sound.");
        assertTrue(commandSource.contains("AddonSounds.LUCAS_DRONE_ENGINE.get()"),
                "The playsoundtest command should use the registered LUCAS engine sound event.");
    }

    @Test
    void droneAudioManagerStartsAudibleDroneLayersAfterTakeoff() throws Exception {
        String source = readJavaSource("DroneAudioManager.java");

        assertTrue(source.contains("collectCandidates(level, player, listener, cubedCandidates, lucasCandidates, deliveryCandidates, glideBombCandidates);"),
                "The drone audio manager should actively scan nearby audible drones each client tick so loops can start after takeoff.");
        assertTrue(source.contains("private static void collectCandidates(ClientLevel level"),
                "The drone audio manager should expose a dedicated nearby-audible drone scan helper.");
        assertTrue(source.contains("for (Entity entity : level.entitiesForRendering())"),
                "The nearby-audible drone scan should iterate the client renderable entities so airborne drones around the player can bootstrap their engine loop.");
        assertTrue(source.contains("entity instanceof CubedFpvDroneEntity"));
        assertTrue(source.contains("entity instanceof LucasDroneEntity"));
        assertTrue(source.contains("entity instanceof DeliveryDroneEntity"));
        assertTrue(source.contains("entity instanceof GlideBombEntity"));
        assertTrue(source.contains("distance > profile.maxDistance()"),
                "The nearby-audible drone scan should still respect each drone profile's maximum audible distance.");
        assertTrue(source.contains("selectAndUpdate("),
                "The nearby-audible drone scan should update persistent layered sounds once a drone becomes audible.");
    }

    @Test
    void droneCrashExplosionSystemGivesLucasDroneADedicatedHeavierBlastProfile() throws Exception {
        String source = readJavaSource("DroneCrashExplosionSystem.java");

        assertTrue(source.contains("LUCAS_CRASH_EXPLOSION_POWER"),
                "The crash explosion system should expose a dedicated heavier blast power for the LUCAS drone.");
        assertTrue(source.contains("boolean lucasDrone = drone instanceof LucasDroneEntity;"),
                "The crash explosion system should branch on the dedicated LUCAS drone type.");
        assertTrue(source.contains("lucasDrone")
                        && source.contains("LUCAS_CRASH_EXPLOSION_POWER")
                        && source.contains("CRASH_EXPLOSION_POWER"),
                "The crash explosion system should still keep a dedicated real explosion power for the LUCAS drone while preserving the base profile.");
        assertTrue(source.contains("boolean glideBomb = drone instanceof GlideBombEntity;"),
                "The crash explosion system should branch on the dedicated Glide Bomb type.");
        assertTrue(source.contains("GLIDE_BOMB_CRASH_EXPLOSION_POWER = FPV_CRASH_EXPLOSION_POWER * 10.0F"),
                "The Glide Bomb crash profile should be ten times stronger than the FPV drone profile.");
        assertTrue(source.contains("spawnCrashEffects(level, drone.position(), fpvDrone, lucasDrone, glideBomb);"),
                "The crash explosion system should route crash particles and sound through a LUCAS-aware effects branch.");
    }

    @Test
    void addonRegistrationsAndCompatibilityIncludeTheLucasDrone() throws Exception {
        String creativeTabsSource = readJavaSource("AddonCreativeTabs.java");
        String renderersSource = readJavaSource("AddonEntityRenderers.java");
        String compatSource = readJavaSource("SbwCompat.java");
        String entitiesSource = readJavaSource("AddonEntities.java");
        String inventorySystemSource = readJavaSource("DroneInventorySystem.java");

        assertTrue(creativeTabsSource.contains("output.accept(AddonItems.LUCAS_DRONE.get());"),
                "The LUCAS drone item should be exposed on the addon creative tab.");
        assertTrue(renderersSource.contains("event.registerEntityRenderer(AddonEntities.LUCAS_DRONE.get(), LucasDroneRenderer::new);"),
                "The LUCAS drone entity should register its dedicated renderer.");
        assertTrue(compatSource.contains("SbwDroneRangeConfig.LUCAS_DRONE_ENTITY_ID.equals(key.toString())"),
                "Shared addon compatibility should treat the LUCAS drone as a supported drone entity.");
        assertTrue(entitiesSource.contains(".sized(3.2F, 0.35F)"),
                "The LUCAS drone entity should use a flatter fixed-wing hitbox registration that is closer to the aircraft.");
        assertTrue(inventorySystemSource.contains("screen.sbwdroneconfig.lucas_drone_inventory"),
                "The drone inventory system should expose a dedicated LUCAS inventory title.");
    }

    private static String readJavaSource(String fileName) throws Exception {
        Path sourcePath = Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", fileName);
        return Files.readString(sourcePath, StandardCharsets.UTF_8);
    }

    private static String readProjectFile(String first, String... more) throws Exception {
        Path sourcePath = Path.of(first, more);
        return Files.readString(sourcePath, StandardCharsets.UTF_8);
    }
}
