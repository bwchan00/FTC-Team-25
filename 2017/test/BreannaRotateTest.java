package test;


import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import opmodes.VioletConstants;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.MecanumWheelDriveTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;

import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;

//import team25core.OneWheelDriveTask;

/**
 * FTC Team 25: Created by Breanna Chan on 11/1/17.
 */
@TeleOp(name = "Rotate Test", group = "Team25")
@Disabled
public class BreannaRotateTest extends Robot {

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

    private DcMotor rotate;


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

        rotate     = hardwareMap.dcMotor.get("rotate");



        rotate.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rotate.setMode(DcMotor.RunMode.RUN_USING_ENCODER);



        // Sets claw servos to open position

        // Allows for linear and slide motor to hold position when no button is pressed

        rotate.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
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


    @Override
    public void start()
    {

        //controlLinear = new OneWheelDriveTask(this, linear, true);

        //this.addTask(controlLinear);

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;


                // Finish a move before we allow another one.

                if (lockout == true) {
                    return;
                }

                if (event.kind == EventKind.BUTTON_B_DOWN) {
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

    }
}