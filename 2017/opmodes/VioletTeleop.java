package opmodes;


import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.MecanumWheelDriveTask;
import team25core.OneWheelDriveTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;

import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;

/**
 * FTC Team 25: Created by Elizabeth Wu on 11/1/17.
 */
@TeleOp(name = "Violet Teleop", group = "Team25")
public class VioletTeleop extends Robot {

     /*

    GAMEPAD 1: DRIVETRAIN CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    |  // FOR FUTURE..needs to be programmed
                                        |  (LT) bward left diagonal    (RT) bward right diagonal
      (L bumper)         (R bumper)     |  (LB) fward left diagonal    (RB) fward right diagonal
                            (y)         |
      arrow pad          (x)   (b)      |
                            (a)         |

    GAMEPAD 2: MECHANISM CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    | (LT) rotate block left      (RT) lower relic holder
      (L bumper)         (R bumper)     | (LB) rotate block right     (RB) raise relic holder
                            (y)         |  (y)
      arrow pad          (x)   (b)      |  (b)
                            (a)         |  (a)

    */

    private enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    }

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor rotate;
    private DcMotor linear;
    private DcMotor slide;

    private Servo s2;
    private Servo s4;
    private Servo s1;
    private Servo s3;
    private Servo jewel;
    private Servo relic;

    private FourWheelDirectDrivetrain drivetrain;
    private MecanumWheelDriveTask drive;
    private OneWheelDriveTask controlLinear;
    private OneWheelDriveTask controlSlide;

    //private boolean clawDown = true;
    private boolean s1Open = true;
    private boolean s3Open = true;
    private boolean relicOpen = true;

    private boolean lockout = false;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof RunToEncoderValueTask.RunToEncoderValueEvent) {
            if (((RunToEncoderValueTask.RunToEncoderValueEvent)e).kind == RunToEncoderValueTask.EventKind.DONE) {
                lockout = false;
                RobotLog.i("Done moving motor");
            }
        }
    }


    @Override
    public void init()
    {
        // Hardware mapping.
        frontLeft  = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft   = hardwareMap.dcMotor.get("rearLeft");
        rearRight  = hardwareMap.dcMotor.get("rearRight");
        rotate     = hardwareMap.dcMotor.get("rotate");
        linear     = hardwareMap.dcMotor.get("linear");
        slide      = hardwareMap.dcMotor.get("slide");

        s2    = hardwareMap.servo.get("s2");
        s4    = hardwareMap.servo.get("s4");
        s1    = hardwareMap.servo.get("s1");
        s3    = hardwareMap.servo.get("s3");
        jewel       = hardwareMap.servo.get("jewel");
        relic       = hardwareMap.servo.get("relic");

        // Sets position of jewel for teleop
        jewel.setPosition(VioletConstants.JEWEL_INIT);

        // Reset encoders.
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rotate.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rotate.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        linear.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        linear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        slide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Allows for rotate, linear, and slide motor to hold position when no button is pressed
        rotate.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        linear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        slide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);

        // Sets claw servos to open position
        openClaw();
    }

    /**
     * Move claw up. Uses a 60 motor.
     */
    private void toggleClawUp()
    {
        linear.setDirection(DcMotorSimple.Direction.FORWARD);

        this.addTask(new RunToEncoderValueTask(this, linear, VioletConstants.CLAW_VERTICAL, VioletConstants.CLAW_VERTICAL_POWER));
    }
    /**
     * Move claw down. Uses a 60 motor.
     */
    private void toggleClawDown()
    {
        linear.setDirection(DcMotorSimple.Direction.REVERSE);

        this.addTask(new RunToEncoderValueTask(this, linear, VioletConstants.CLAW_VERTICAL, VioletConstants.CLAW_VERTICAL_POWER));
    }

    /**
     * Blindly open both claws and set the state appropriately.
     */
    private void openClaw()
    {
        s1.setPosition(VioletConstants.S1_OPEN);
        s2.setPosition(VioletConstants.S2_OPEN);
        s3.setPosition(VioletConstants.S3_OPEN);
        s4.setPosition(VioletConstants.S4_OPEN);

        s1Open = true;
        s3Open = true;
    }

    /**
     * The servos always work in pairs.  S1/S2 and S3/S4.  toggleS1 therefore refers the to the S1/S2 pair.
     */
    private void toggleS1()
    {
        if (s1Open == true) {
            s1.setPosition(VioletConstants.S1_CLOSED);
            s2.setPosition(VioletConstants.S2_CLOSED);
            s1Open = false;
        } else {
            s1.setPosition(VioletConstants.S1_OPEN);
            s2.setPosition(VioletConstants.S2_OPEN);
            s1Open = true;
        }
    }

    /**
     * The servos always work in pairs.  S1/S2 and S3/S4.  toggleS3 therefore refers the to the S3/S4 pair.
     */
    private void toggleS3()
    {
        if (s3Open == true) {
            s3.setPosition(VioletConstants.S3_CLOSED);
            s4.setPosition(VioletConstants.S4_CLOSED);
            s3Open = false;
        } else {
            s3.setPosition(VioletConstants.S3_OPEN);
            s4.setPosition(VioletConstants.S4_OPEN);
            s3Open = true;
        }
    }

    /**
     * We will spin the claw back and forth, be careful that you alternate directions so that
     * you don't wrap the servo cables around the motor shaft.
     *
     * This motor's movement is not symmetrical, so we'll compensate in one direction.
     */
    private void rotate(Direction direction)
    {
        int distance;

        if (direction == Direction.CLOCKWISE) {
            rotate.setDirection(DcMotorSimple.Direction.REVERSE);
            distance = VioletConstants.DEGREES_180;
        } else {
            rotate.setDirection(DcMotorSimple.Direction.FORWARD);
            distance = VioletConstants.DEGREES_180;
        }
        this.addTask(new RunToEncoderValueTask(this, rotate, VioletConstants.DEGREES_180, VioletConstants.ROTATE_POWER));
    }

    /**
     * Fine alignment for the claw.  Note that there is no deadman motor function specifically to
     * avoid operator error wherein the motor is held on too long and we over rotate thereby
     * damaging the cabling or wire harnesses.
     */
    private void nudge(Direction direction)
    {
        if (direction == Direction.CLOCKWISE) {
            rotate.setDirection(DcMotorSimple.Direction.REVERSE);
        } else {
            rotate.setDirection(DcMotorSimple.Direction.FORWARD);
        }
        this.addTask(new RunToEncoderValueTask(this, rotate, VioletConstants.NUDGE, VioletConstants.NUDGE_POWER));
    }

    /**
     * Extend relic mechanism out. Uses a 40 motor.
     */
    private void extendRelic()
    {
        slide.setDirection(DcMotorSimple.Direction.FORWARD);

        this.addTask(new RunToEncoderValueTask(this, slide, VioletConstants.RELIC_HORIZONTAL, VioletConstants.RELIC_HORIZONTAL_POWER));
    }

    /**
     * Bring relic mechanism back in. Uses a 40 motor.
     */
    private void contractRelic()
    {
        slide.setDirection(DcMotorSimple.Direction.REVERSE);

        this.addTask(new RunToEncoderValueTask(this, slide, VioletConstants.RELIC_HORIZONTAL, VioletConstants.RELIC_HORIZONTAL_POWER));
    }

    /**
     * Opens and closes relic servo.
     */
    private void toggleRelic()
    {
        if (relicOpen == true) {
            relic.setPosition(VioletConstants.RELIC_CLOSED);
            relicOpen= false;
        } else {
            relic.setPosition(VioletConstants.RELIC_OPEN);
            relicOpen = true;
        }
    }

    @Override
    public void start()
    {
        drive = new MecanumWheelDriveTask(this, frontLeft, frontRight, rearLeft, rearRight);
        controlLinear = new OneWheelDriveTask(this, linear, true);
        controlSlide = new OneWheelDriveTask(this, slide, false);
        this.addTask(drive);
        this.addTask(controlLinear);
        this.addTask(controlSlide);

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;


                // Finish a move before we allow another one.

                if (lockout == true) {
                    return;
                }

                if (event.kind == EventKind.BUTTON_Y_DOWN) {
                    // Lifts glyph mechanism

                    toggleClawUp();
                    linear.setPower(VioletConstants.CLAW_VERTICAL_POWER);
                } else if (event.kind == EventKind.BUTTON_A_DOWN) {
                    // Lowers glyph mechanism

                    toggleClawDown();
                    linear.setPower(VioletConstants.CLAW_VERTICAL_POWER);
                    //openClaw();
                } else if (event.kind == EventKind.LEFT_BUMPER_DOWN) {
                    // Toggle s1/s2

                    toggleS1();
                } else if (event.kind == EventKind.RIGHT_BUMPER_DOWN) {
                    // Toggle s3/s4

                    toggleS3();
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    // Rotate 180 degrees clockwise looking from behind robot

                    lockout = true;
                    rotate(Direction.CLOCKWISE);
                } else if (event.kind == EventKind.BUTTON_X_DOWN) {
                    // Rotate 180 degrees counterclockwise looking from behind robot

                    lockout = true;
                    rotate(Direction.COUNTERCLOCKWISE);
                } else if (event.kind == EventKind.LEFT_TRIGGER_DOWN) {
                    // Nudge counterclockwise looking from behind robot

                    lockout = true;
                    nudge(Direction.COUNTERCLOCKWISE);
                } else if (event.kind == EventKind.RIGHT_TRIGGER_DOWN) {
                    // Nudge clockwise looking from behind robot

                    lockout = true;
                    nudge(Direction.CLOCKWISE);
                }
            }
        });

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;


                if (event.kind == EventKind.LEFT_BUMPER_DOWN) {
                    // Extends relic mechanism

                    extendRelic();
                    slide.setPower(VioletConstants.RELIC_HORIZONTAL_POWER);
                } else if (event.kind == EventKind.RIGHT_BUMPER_DOWN) {
                    // Brings relic mechanism back in

                    contractRelic();
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    // Toggle relic servo

                    toggleRelic();
                }
            }
        });
    }
}