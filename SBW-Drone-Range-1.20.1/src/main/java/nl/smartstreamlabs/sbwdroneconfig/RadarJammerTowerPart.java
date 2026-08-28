package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum RadarJammerTowerPart implements StringRepresentable {
    WEST_TWO_NORTH("west_two_north", -2, -1),
    NORTH_WEST("north_west", -1, -1),
    NORTH("north", 0, -1),
    EAST_NORTH("east_north", 1, -1),
    WEST_TWO("west_two", -2, 0),
    WEST("west", -1, 0),
    EAST("east", 1, 0);

    public static final int FOOTPRINT_WIDTH_BLOCKS = 4;
    public static final int FOOTPRINT_DEPTH_BLOCKS = 2;

    private final String serializedName;
    private final int xOffset;
    private final int zOffset;

    RadarJammerTowerPart(String serializedName, int xOffset, int zOffset) {
        this.serializedName = serializedName;
        this.xOffset = xOffset;
        this.zOffset = zOffset;
    }

    public BlockPos partPos(BlockPos masterPos, Direction facing) {
        int[] rotated = rotateOffset(xOffset, zOffset, facing);
        return masterPos.offset(rotated[0], 0, rotated[1]);
    }

    public BlockPos masterPos(BlockPos partPos, Direction facing) {
        int[] rotated = rotateOffset(xOffset, zOffset, facing);
        return partPos.offset(-rotated[0], 0, -rotated[1]);
    }

    private static int[] rotateOffset(int xOffset, int zOffset, Direction facing) {
        return switch (facing) {
            case EAST -> new int[] {-zOffset, xOffset};
            case SOUTH -> new int[] {-xOffset, -zOffset};
            case WEST -> new int[] {zOffset, -xOffset};
            default -> new int[] {xOffset, zOffset};
        };
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
