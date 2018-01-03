package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;
import com.vuforia.CameraDevice;
import com.vuforia.VuMarkTarget;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

import team25core.ColorThiefTask;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;
import team25core.VuMarkIdentificationTask;
import team25core.VuforiaBase;

/*
 * FTC Team 25: Created by Elizabeth Wu on 10/28/17.
 */

@Autonomous(name = "Violet Jewel Autonomous", group = "Team 25")
public class VioletJewelAutonomous extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor rotate;
    private Servo jewel;
    private ColorThiefTask colorThiefTask;
    private DeviceInterfaceModule cdim;
    private Alliance alliance;
    private Position position;
    private Side side;
    private DeadReckonPath park;
    private DeadReckonPath pushJewel;
    private DeadReckonTask task;
    private SingleShotTimerTask stt;
    private SingleShotTimerTask moveDelay;
    private GlyphAutonomousPathUtility utility;
    private GlyphAutonomousPathUtility.StartStone stonePosition;
    private GlyphAutonomousPathUtility.TargetColumn targetColumn;
    private RelicRecoveryVuMark vuMark;
    private VuMarkIdentificationTask vmIdTask;
    private VuforiaBase vuforiaBase;

    private GlyphAutonomousPathUtility.TargetColumn tgtColumn = GlyphAutonomousPathUtility.TargetColumn.LEFT;


    private Telemetry.Item particle;
    private Telemetry.Item allianceItem;
    private Telemetry.Item positionItem;
    private Telemetry.Item pollItem;
    private Telemetry.Item flashItem;
    private Telemetry.Item vuMarkItem;

    boolean flashOn = false;
    boolean pollOn = false;

    private static final int TURN_MULTIPLIER = -1;
    private int distance = 0;
    private int whichSide = 0;
    private int combo = 0;
    private int color = 0;
    private int liftJewel = 0;
    private int isBlack = 0;

    // Park combos.
    private static final int BLUE_FAR = 0;
    private static final int RED_FAR = 1;
    private static final int BLUE_NEAR = 2;
    private static final int RED_NEAR = 3;

    private FourWheelDirectDrivetrain drivetrain;

    public enum Alliance {
        RED,
        BLUE,
    }

    public enum Position {
        NEAR,
        FAR,
    }

    public enum Side {
        LEFT,
        RIGHT,
    }

    @Override
    public void init() {
        telemetry.setAutoClear(false);


        // Hardware mapping.
        frontLeft   = hardwareMap.dcMotor.get("frontLeft");
        frontRight  = hardwareMap.dcMotor.get("frontRight");
        rearLeft    = hardwareMap.dcMotor.get("rearLeft");
        rearRight   = hardwareMap.dcMotor.get("rearRight");
        jewel       = hardwareMap.servo.get("jewel");


        // Telemetry setup.
        telemetry.setAutoClear(false);
        allianceItem    = telemetry.addData("ALLIANCE", "Unselected (X/B)");
        positionItem    = telemetry.addData("POSITION", "Unselected (Y/A)");
        particle        = telemetry.addData("Particle: ", "No data");
        vuMarkItem      = telemetry.addData("VuMark: ", "No data");

        // Path setup.
        park = new DeadReckonPath();

        // Arm initialized up
        jewel.setPosition(VioletConstants.JEWEL_UP);    // 145/256

        // Single shot timer tasks for delays.
        stt = new SingleShotTimerTask(this, 1500);          // Delay resetting arm position
        moveDelay = new SingleShotTimerTask(this, 500);     // Delay moving after setting arm down.

        // Alliance and autonomous choice selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        drivetrain.setNoncanonicalMotorDirection();

        RobotLog.i("506 init: before new GlyphAutonomousPathUtility")
        utility = new GlyphAutonomousPathUtility();

        RobotLog.i("506 init: after new GlyphAutonomousPathUtility")
        sense();
       // detectVuMark();
      //  vuforiaBase.setCameraDirection(VuforiaLocalizer.CameraDirection.BACK);
    }

    public void start()
    {
        // Arm goes down
        jewel.setPosition(VioletConstants.JEWEL_DOWN);
        // 15/256
        addTask(new SingleShotTimerTask(this, 500) {
                    @Override
                    // This handleEvent occurs after half a second passes to raise the arm.
                    public void handleEvent(RobotEvent e) {
                        robot.addTask(new DeadReckonTask(robot, pushJewel, drivetrain) {
                            @Override
                            // This handleEvent occurs after the pushJewel runs.
                            public void handleEvent(RobotEvent e) {
                                DeadReckonEvent path = (DeadReckonEvent) e;
                                if (path.kind == EventKind.PATH_DONE) {

                                    /*addTask(new DeadReckonTask(robot, park, drivetrain) {
                                        @Override
                                        public void handleEvent(RobotEvent e) {
                                            DeadReckonEvent path = (DeadReckonEvent) e;
                                        }
                                    });
                                    */
                                    RobotLog.i("506 start: before detectVuMark");
                                    targetColumn = detectVuMark(robot);
                                    RobotLog.i("506 start: after detectVuMark");
                                    // FIXME: jewel sensing and knock off works but glyph paths throw NullPointerException
                                    vuMarkItem.setValue(vuMark.toString());
                                    park = utility.getPath(targetColumn, stonePosition);
                                    RobotLog.i("506 start: after utility.getPath");
                                    robot.addTask(new DeadReckonTask(robot, park, drivetrain));
                                    RobotLog.i("506 start: after addTask DeadReckonTask");
                                }
                            }
                        });
                        robot.addTask(new SingleShotTimerTask(robot, 500) {
                            @Override
                            public void handleEvent(RobotEvent e) {
                                jewel.setPosition(VioletConstants.JEWEL_UP);
                            }
                        });
                    }
                });

    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            //RobotLog.i("Jewel: Detected " + e.toString());

            switch (event.kind) {
                case BUTTON_X_DOWN:
                    selectAlliance(Alliance.BLUE);
                    allianceItem.setValue("Blue");
                    break;
                case BUTTON_B_DOWN:
                    selectAlliance(Alliance.RED);
                    allianceItem.setValue("Red");
                    break;
                case BUTTON_Y_DOWN:
                    selectPosition(Position.FAR);
                    positionItem.setValue("Far");
                    break;
                case BUTTON_A_DOWN:
                    selectPosition(Position.NEAR);
                    positionItem.setValue("Near");
                    break;
                case RIGHT_BUMPER_DOWN:
                    togglePolling();
                    break;
                default:
                    break;
            }
        } // setupParkPath();
    }

    private void togglePolling() {
        if (pollOn == false) {
            colorThiefTask.setPollingMode(ColorThiefTask.PollingMode.ON);
            vmIdTask.setPollingMode(VuMarkIdentificationTask.PollingMode.ON);
            pollOn = true;

        } else {
            colorThiefTask.setPollingMode(ColorThiefTask.PollingMode.OFF);
            vmIdTask.setPollingMode(VuMarkIdentificationTask.PollingMode.OFF);
            pollOn = false;
        }
    }


    private void sense()
    {
        VuforiaBase vuforiaBase = new VuforiaBase();
        vuforiaBase.init(this);
        vuforiaBase.setCameraDirection(VuforiaLocalizer.CameraDirection.FRONT);

        colorThiefTask = new ColorThiefTask(this, vuforiaBase) {
            @Override
            public void handleEvent(RobotEvent e) {
                ColorThiefTask.ColorThiefEvent event = (ColorThiefEvent) e;
                particle.setValue(event.toString());
                pushJewel = new DeadReckonPath();

                if (event.kind == EventKind.BLACK) {
                    isBlack = 1;
                } else {
                    if (alliance == Alliance.RED) {
                        if (event.kind == EventKind.RED) {
                            pushJewel.stop();
                            RobotLog.i("506 Sensed RED");
                            pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 30, Violet.TURN_SPEED);
                            pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 30, Violet.TURN_SPEED * TURN_MULTIPLIER);
                            // FIXME: Need to add a delay to the last jewel segment because it keeps on pushing the jewel off.
                            pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 11, Violet.STRAIGHT_SPEED);
                            liftJewel = 1;
                        } else {
                            RobotLog.i("506 Sensed BLUE");
                            pushJewel.stop();
                           // pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 7, Violet.STRAIGHT_SPEED);
                            liftJewel = 1;
                        }
                    } else if (alliance == Alliance.BLUE) {
                        if (event.kind == EventKind.BLUE) {
                            RobotLog.i("506 Sensed BLUE");
                            pushJewel.stop();
                           // pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 7, Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                            liftJewel = 1;
                        } else {
                            RobotLog.i("506 Sensed RED");
                            pushJewel.stop();
                            pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 30, Violet.TURN_SPEED * TURN_MULTIPLIER);
                            pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 30, Violet.TURN_SPEED);
                            liftJewel = 1;
                            pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 11, Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                        }
                    }
                }
            }
        };

        addTask(colorThiefTask);
    }

    private GlyphAutonomousPathUtility.TargetColumn detectVuMark(Robot robot)
    {
        //vmIdTask = new VuMarkIdentificationTask(robot, vuforiaBase);
        RobotLog.i("506 added VuMark ID task");
        robot.addTask(new VuMarkIdentificationTask(robot, vuforiaBase) {
            @Override
            public void handleEvent(RobotEvent e) {
                VuMarkIdentificationTask.VuMarkIdentificationEvent position = (VuMarkIdentificationTask.VuMarkIdentificationEvent) e;
                switch (position.kind) {
                    case CENTER:
                        tgtColumn = GlyphAutonomousPathUtility.TargetColumn.CENTER;
                        break;
                    case LEFT:
                        tgtColumn = GlyphAutonomousPathUtility.TargetColumn.LEFT;
                        break;
                    case RIGHT:
                        tgtColumn = GlyphAutonomousPathUtility.TargetColumn.RIGHT;
                        break;
                    default:
                        RobotLog.i("506 Detect VuMark invalid position kind:", position.kind);
                        break;
                }

            }
        });
        return tgtColumn;
    }

    private void selectAlliance(Alliance color) {
        if (color == Alliance.BLUE) {
            // Blue setup.
            RobotLog.i("506 Alliance: BLUE");
            alliance = Alliance.BLUE;
        } else {
            // Red setup.
            RobotLog.i("506 Alliance: RED");
            alliance = Alliance.RED;
        }
    }

    public void selectPosition(Position choice) {
        if (choice == Position.FAR) {
            position = Position.FAR;
            RobotLog.i("506 Position: FAR");
        } else {
            position = Position.NEAR;
            RobotLog.i("506 Position: NEAR");
        }
    }

    private void getStonePosition()
    {
        if (alliance == Alliance.RED) {
            color = 1;
        } else if (alliance == Alliance.BLUE) {
            color = 0;
        }

        if (position == Position.NEAR) {
            distance = 2;
        } else {
            distance = 0;
        }

        combo = color + distance;

        switch (combo) {
            case BLUE_FAR:
                stonePosition = GlyphAutonomousPathUtility.StartStone.BLUE_FAR;
                break;
            case RED_FAR:
                stonePosition = GlyphAutonomousPathUtility.StartStone.RED_FAR;
                break;
            case BLUE_NEAR:
                stonePosition = GlyphAutonomousPathUtility.StartStone.BLUE_NEAR;
                break;
            case RED_NEAR:
                stonePosition = GlyphAutonomousPathUtility.StartStone.RED_NEAR;
                break;
            default:
                break;

        }
    }


  /*  private void setupParkPath()
    {


        if (alliance == Alliance.RED) {
            color = 1;
        } else if (alliance == Alliance.BLUE) {
            color = 0;
        }

        if (position == Position.NEAR) {
            distance = 2;
        } else {
            distance = 0;
        }

        combo = color + distance;

         // + whichSide;

        switch (combo) {
            case BLUE_FAR:
                RobotLog.i("506 Case: BLUE_FAR");
                park.stop();
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 27, Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 10, Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                break;
            case RED_FAR:
                RobotLog.i("506 Case: RED_FAR");
                park.stop();
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 24, Violet.STRAIGHT_SPEED);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 13 , Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                break;
            case BLUE_NEAR:
                RobotLog.i("506 Case: BLUE_NEAR");
                park.stop();
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 18, Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 7, Violet.STRAIGHT_SPEED);
                break;
            case RED_NEAR:
                RobotLog.i("506 Case: RED_NEAR");
                park.stop();

                // TODO: make the straight and side segments further

                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20, Violet.STRAIGHT_SPEED);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 9, Violet.STRAIGHT_SPEED);
                break;
            default:
                break;
        }
    } */

}

