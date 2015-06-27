package lns.ev3.model;

import lejos.hardware.motor.Motor;

public class Ev3 {
	private int xp;
	private int yp;
	private int speed;
	private int direction = 0; //0==straight, -1==left, 1==right
	private int targetXp;
	private int targetYp;
	
	public Ev3() {
		setSpeed(100);
	}
	public void ev3SetUp(int x, int y, int speed,int tx, int ty) {
		xp = x;
		yp = y;
		targetXp = tx;
		targetYp = ty;
		speed = this.speed;
		
	}
	public boolean straightOneBlock() {
		if ( direction == 0 && yp == 3)
			return false;
		else if ( direction == -1 && xp == 0)
			return false;
		else if ( direction == 1  && xp == 4)
			return false;
		
		Motor.B.rotate(430, true);
		Motor.C.rotate(430);
		
		if( direction == 0 ) {
			yp++;
		} else {
			xp += direction;
		}
		return true;
	}

	public boolean turnRight() {
		int degree = 90;
		if (speed == 0 || direction==1)
			return false;
		Motor.B.rotate(degree * 2, true);
		Motor.C.rotate(-degree * 2);
		direction += 1;
		return true;
	}

	public boolean turnLeft() {
		int degree = 90;
		if (speed == 0 || direction==-1)
			return false;
		Motor.B.rotate(-degree * 2, true);
		Motor.C.rotate(degree * 2);
		direction -= 1;
		return true;
	}

//	public int getOneBlockDist() {
//		return 360*speed/100;
//	}

	public int getXp() {
		return xp;
	}

	public void setXp(int xp) {
		this.xp = xp;
	}

	public int getYp() {
		return yp;
	}

	public void setYp(int yp) {
		this.yp = yp;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		Motor.B.setSpeed(speed);
		Motor.C.setSpeed(speed);
		this.speed = speed;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	public int getTargetXp() {
		return targetXp;
	}
	public void setTargetXp(int targetXp) {
		this.targetXp = targetXp;
	}
	
	public int getTargetYp() {
		return targetYp;
	}
	public void setTargetYp(int targetYp) {
		this.targetYp = targetYp;
	}
}