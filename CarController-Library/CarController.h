#ifndef CARCONTROLLER_H //include guard
#define CARCONTROLLER_H 

#include "Arduino.h"

/**
Whole class for controlling smart robot car*/
class CarController{
private:

	/**
	Class for controlling one DC motor*/
	class DCMotorController{
	private:
		//value: the duty cycle: between 0 (always off) and 255 (always on) - unsigned char: 0 to 255
		unsigned char in1;
		unsigned char in2;
		unsigned char en1;
	public:
		/**
		Constructor
		@param input1 digital pin for direction
		@param input2 ditigal pin for direction
		@param enable analog pin, PWM, for speed
		*/
		DCMotorController(unsigned char input1, unsigned char input2, unsigned char enable);
		~DCMotorController();

		void forward(unsigned char speed);
		void backward(unsigned char speed);
		void stop();

		void setValues(unsigned char input1, unsigned char input2, unsigned char enable);
	};

	DCMotorController *leftMotor, *rightMotor;
public:
	/**
	@param input1 digital pin, control direction
	@param input2 digital pin, control direction
	@param enable1 pwm pin, speed control
	@param enable2 pwm pin, speed control*/
	CarController(unsigned char input1, unsigned char input2, unsigned char enable1, unsigned char input3, unsigned char input4, unsigned char enable2);
	~CarController();

	/**
	@param speed number between 0 and 255
	*/
	void forward(unsigned char speed);

	/**
	@param speed number between 0 and 255
	*/
	void backward(unsigned char speed);

	/**
	@param speed number between 0 and 255
	*/
	void rotateLeft(unsigned char speed);

	/**
	Use only right motor for move*/
	void turnLeft(unsigned char speed);

	/**
	@param speed number between 0 and 255
	*/
	void rotateRight(unsigned char speed);

	/**
	Use only left motor for move*/
	void turnRight(unsigned char speed);

	/***/
	void stop();

	/**
	Function to move on car with delay
	@param direction one letter: 'F' - forward, 'B' - backward, 'L' - left, 'R' - right
	@param speed number between 0 and 255
	@param delay how many miliseconds will motors work
	*/
	void moveOnWithDelay(char* direction, unsigned char speed = 0, int delay = 500);

	/**
	Make move without delay, move and stop when direction is equal to 'S' */
	void moveOn(char* direction, unsigned char speed = 0);
};
#endif