#pragma config(Hubs,  S1, HTMotor,  HTServo,  none,     none)
#pragma config(Sensor, S1,     ,               sensorI2CMuxController)
#pragma config(Sensor, S2,     irr_right,      sensorI2CCustom)
#pragma config(Sensor, S3,     irr_left,       sensorI2CCustom)
#pragma config(Motor,  mtr_S1_C1_1,     driveRearRight, tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C1_2,     driveRearLeft, tmotorTetrix, PIDControl, reversed, encoder)
#pragma config(Servo,  srvo_S1_C2_1,    leftEye,              tServoStandard)
#pragma config(Servo,  srvo_S1_C2_2,    rightEye,             tServoStandard)
#pragma config(Servo,  srvo_S1_C2_3,    servo3,               tServoNone)
#pragma config(Servo,  srvo_S1_C2_4,    servo4,               tServoNone)
#pragma config(Servo,  srvo_S1_C2_5,    servo5,               tServoNone)
#pragma config(Servo,  srvo_S1_C2_6,    servo6,               tServoNone)

task main()
{
    motor[driveRearRight] = 50;
    motor[driveRearLeft] = 50;

    while (true) {}
}
