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
    EAST("east", 1, 0),
    CELL_M1_0_1("cell_m1_0_1", -1, 0, 1),
    CELL_0_0_1("cell_0_0_1", 0, 0, 1),
    CELL_1_0_1("cell_1_0_1", 1, 0, 1),
    CELL_M1_1_M1("cell_m1_1_m1", -1, 1, -1),
    CELL_M1_1_0("cell_m1_1_0", -1, 1, 0),
    CELL_M1_1_1("cell_m1_1_1", -1, 1, 1),
    CELL_0_1_M1("cell_0_1_m1", 0, 1, -1),
    CELL_0_1_0("cell_0_1_0", 0, 1, 0),
    CELL_0_1_1("cell_0_1_1", 0, 1, 1),
    CELL_1_1_M1("cell_1_1_m1", 1, 1, -1),
    CELL_1_1_0("cell_1_1_0", 1, 1, 0),
    CELL_1_1_1("cell_1_1_1", 1, 1, 1),
    CELL_M1_2_M1("cell_m1_2_m1", -1, 2, -1),
    CELL_M1_2_0("cell_m1_2_0", -1, 2, 0),
    CELL_M1_2_1("cell_m1_2_1", -1, 2, 1),
    CELL_0_2_M1("cell_0_2_m1", 0, 2, -1),
    CELL_0_2_0("cell_0_2_0", 0, 2, 0),
    CELL_0_2_1("cell_0_2_1", 0, 2, 1),
    CELL_1_2_M1("cell_1_2_m1", 1, 2, -1),
    CELL_1_2_0("cell_1_2_0", 1, 2, 0),
    CELL_1_2_1("cell_1_2_1", 1, 2, 1);

    public static final int FOOTPRINT_WIDTH_BLOCKS = 4;
    public static final int FOOTPRINT_DEPTH_BLOCKS = 2;

    private final String serializedName;
    private final int yOffset;
    private final int xOffset;
    private final int zOffset;

    RadarJammerTowerPart(String serializedName, int xOffset, int zOffset) {
        this(serializedName,xOffset,0,zOffset);
    }

    RadarJammerTowerPart(String serializedName,int xOffset,int yOffset,int zOffset) {
        this.yOffset=yOffset;
        this.serializedName = serializedName;
        this.xOffset = xOffset;
        this.zOffset = zOffset;
    }

    public BlockPos partPos(BlockPos masterPos, Direction facing) {
        int[] rotated = rotateOffset(xOffset, zOffset, facing);
        return masterPos.offset(rotated[0], yOffset, rotated[1]);
    }

    public BlockPos masterPos(BlockPos partPos, Direction facing) {
        int[] rotated = rotateOffset(xOffset, zOffset, facing);
        return partPos.offset(-rotated[0], -yOffset, -rotated[1]);
    }

    public BlockPos localOffset(){return new BlockPos(xOffset,yOffset,zOffset);}
    public boolean required(){return !Rj01Shapes.cell(net.minecraft.core.Direction.NORTH,localOffset()).isEmpty();}
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
