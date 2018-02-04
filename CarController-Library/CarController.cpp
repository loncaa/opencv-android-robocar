#include "CarController.h"

CarController::CarController(unsigned char input1, unsigned char input2, unsigned char enable1, unsigned char input3, unsigned char input4, unsigned char enable2)
{
	leftMotor = new DCMotorController(input1, input2, enable1);
	rightMotor = new DCMotorController(input3, input4, enable2);
}
CarController::~CarController()
{
	delete leftMotor;
	delete rightMotor;
}

void CarController::forward(unsigned char speed)
{
	leftMotor->forward(speed);
	rightMotor->forward(speed);
}
void CarController::backward(unsigned char speed)
{
	leftMotor->backward(speed);
	rightMotor->backward(speed);
}

void CarController::rotateLeft(unsigned char speed)
{
	leftMotor->backward(speed);
	rightMotor->forward(speed);
}
void CarController::turnLeft(unsigned char speed)
{
	leftMotor->stop();
	rightMotor->forward(speed);
}
void CarController::rotateRight(unsigned char speed)
{
	leftMotor->forward(speed);
	rightMotor->backward(speed);
}
void CarController::turnRight(unsigned char speed)
{
	leftMotor->forward(speed);
	rightMotor->stop();
}
void CarController::stop()
{
	leftMotor->stop();
	rightMotor->stop();
}

void CarController::moveOnWithDelay(char* direction, unsigned char speed, int delayn)
{
	if (!strcmp(direction, "F"))
	{
		forward(speed);
		delay(delayn);
	}
	else if (!strcmp(direction, "B"))
	{
		backward(speed);
		delay(delayn);
	}
	else if (!strcmp(direction, "RL"))
	{
		rotateLeft(speed);
		delay(delayn);
	}
	else if (!strcmp(direction, "TL"))
	{
		turnLeft(speed);
		delay(delayn);
	}
	else if (!strcmp(direction, "RR"))
	{
		rotateRight(speed);
		delay(delayn);
	}
	else if (!strcmp(direction, "TR"))
	{
		turnRight(speed);
		delay(delayn);
	}

	stop();
}

void CarController::moveOn(char* direction, unsigned char speed)
{
	if (!strcmp(direction, "F"))
	{
		forward(speed);
	}
	else if (!strcmp(direction, "B"))
	{
		backward(speed);
	}
	else if (!strcmp(direction, "S"))
	{
		stop();
	}
	else if (!strcmp(direction, "RL"))
	{
		rotateLeft(speed);
	}
	else if (!strcmp(direction, "TL"))
	{
		turnLeft(speed);
	}
	else if (!strcmp(direction, "RR"))
	{
		rotateRight(speed);
	}
	else if (!strcmp(direction, "TR"))
	{
		turnRight(speed);
	}
}

CarController::DCMotorController::DCMotorController(unsigned char input1, unsigned char input2, unsigned char enable)
{
	pinMode(enable, OUTPUT);
	pinMode(input1, OUTPUT);
	pinMode(input2, OUTPUT);

	en1 = enable;
	in1 = input1;
	in2 = input2;
}

CarController::DCMotorController::~DCMotorController(){}

void CarController::DCMotorController::setValues(unsigned char input1, unsigned char input2, unsigned char enable)
{
	pinMode(enable, OUTPUT);
	pinMode(input1, OUTPUT);
	pinMode(input2, OUTPUT);

	en1 = enable;
	in1 = input1;
	in2 = input2;
}

void CarController::DCMotorController::forward(unsigned char speed)
{
	analogWrite(en1, speed); //brzina

	digitalWrite(in1, HIGH); //smjer kretanja
	digitalWrite(in2, LOW); //smjer kretanja
}

void CarController::DCMotorController::backward(unsigned char speed)
{
	analogWrite(en1, speed);

	digitalWrite(in1, LOW);
	digitalWrite(in2, HIGH);
}

void CarController::DCMotorController::stop()
{
	analogWrite(en1, 0);

	digitalWrite(in1, LOW);
	digitalWrite(in2, LOW);
}
