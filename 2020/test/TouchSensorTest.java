package test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;

import opmodes.TouchSensorCriteria;
import opmodes.calibration.MiyazakiCalibration;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.TankMechanumControlScheme;
import team25core.TeleopDriveTask;

@TeleOp(name = "Touch Sensor Test")
public class TouchSensorTest extends Robot {
    // drivetrain and servos
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private Servo leftArm;
    private Servo rightArm;
    private TeleopDriveTask driveTask;
    private MechanumGearedDrivetrain drivetrain;

    // touch sensor
    private TouchSensor touchRight;
    private TouchSensor touchLeft;
    private TouchSensorCriteria touchRightCriteria;
    private TouchSensorCriteria touchLeftCriteria;

    // auto path
    private DeadReckonPath touchPath;

    public void handleEvent(RobotEvent e) {

    }

    public void init() {
        // drivetrain, and servos
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");
        leftArm = hardwareMap.get(Servo.class, "leftArm");
        rightArm  = hardwareMap.get(Servo.class, "rightArm");
        drivetrain = new MechanumGearedDrivetrain(60, frontRight, backRight, frontLeft, backLeft);
        drivetrain.setNoncanonicalMotorDirection();
        drivetrain.encodersOn();
        drivetrain.resetEncoders();
        TankMechanumControlScheme scheme = new TankMechanumControlScheme(gamepad1, TankMechanumControlScheme.MotorDirection.NONCANONICAL);
        driveTask = new TeleopDriveTask(this, scheme, frontLeft, frontRight, backLeft, backRight);

        // touch sensor
        touchRight = hardwareMap.get(TouchSensor.class, "touchRight");
        touchLeft = hardwareMap.get(TouchSensor.class, "touchLeft");
        touchRightCriteria = new TouchSensorCriteria(touchRight);
        touchLeftCriteria = new TouchSensorCriteria(touchLeft);

        // touch path
        touchPath = new DeadReckonPath();
        touchPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, 0.2);
    }
    @Override
    public void start() {
        this.addTask(driveTask);
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;
                switch (event.kind) {
                    case BUTTON_X_DOWN:
                    latchFoundation();
                    break;
                }
            }
        });
    }

    public void latchFoundation() {
       this.addTask(new DeadReckonTask(this, touchPath, drivetrain, touchLeftCriteria, touchRightCriteria) {
           public void handleEvent(RobotEvent e) {
               DeadReckonEvent event = (DeadReckonEvent) e;
               switch (event.kind) {
                   case BOTH_SENSORS_SATISFIED:
                       dropFoundationArms(true);
                       break;
               }
           }
       });
    }


    public void dropFoundationArms (boolean drop) {
        if (drop == true) {
            leftArm.setPosition(MiyazakiCalibration.ARM_LEFT_DOWN);
            rightArm.setPosition(MiyazakiCalibration.ARM_RIGHT_DOWN);
        }
        if (drop == false) {
            leftArm.setPosition(MiyazakiCalibration.ARM_LEFT_STOW);
            rightArm.setPosition(MiyazakiCalibration.ARM_RIGHT_STOW);
        }
    }
}
