// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.SpeedControllerGroup;

import edu.wpi.first.wpilibj.controller.PIDController;



/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  /*---Network Table Variables---*/
  NetworkTableEntry distanceEntry;
  NetworkTableEntry orientationEntry;
  double distance;
  double orientation;

  /*---Drive train variables---*/
  VictorSP leftTop;
  VictorSP leftBottom;
  VictorSP rightTop;
  VictorSP rightBottom;
  SpeedControllerGroup m_left;
  SpeedControllerGroup m_right;
  DifferentialDrive m_drive;

  /*---PID---*/
  PIDController pid;


  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    // Motors and Drivetrain setup
    leftTop = new VictorSP(0);
    leftBottom = new VictorSP(1);
    rightTop = new VictorSP(2);
    rightBottom = new VictorSP(3);
    m_left = new SpeedControllerGroup(leftTop, leftBottom);
    m_right = new SpeedControllerGroup(rightTop, rightBottom);
    m_drive = new DifferentialDrive(m_left, m_right);


    // Network Table Setup
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    NetworkTable table = inst.getTable("Vision");
    distanceEntry = table.getEntry("distance");
    orientationEntry = table.getEntry("orientation");
    distanceEntry.addListener(event -> {
      distance = Double.parseDouble(event.value.getValue().toString());
      System.out.println("Distance: " + event.value.getValue());
    }, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);
    orientationEntry.addListener(event -> {
      orientation = Double.parseDouble(event.value.getValue().toString());
      System.out.println("Orientation: " + event.value.getValue());
    }, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);

    // PID setup
    double kp = 0.5;
    double ki = 0;
    double kd = 0;
    pid = new PIDController(kp, ki, kd);


  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {}

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }

    


  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {}

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    if (distance == -1) {
      searchForBall();
      centerBall();
      intakeBall();
    }
    
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /*---Vision Ball Search Functions---*/
  public void searchForBall() {
    if(distance == -1) {
      m_drive.tankDrive(0.5, -0.5);
      Timer.delay(0.2);
      m_drive.tankDrive(0, 0);
    }
  }

  public void centerBall() {
    // Adjust orientation using PID
    while (orientation-0 > 50) {
      double process = pid.calculate(orientation, 0); // orientation of the ball should always be at center (0)
      m_drive.tankDrive(process, -process);
    }
    
  }

  public void intakeBall() {
    while (distance > 10) {
      m_drive.tankDrive(0.5, 0.5);
    }
    m_drive.tankDrive(0, 0);
  }
}
