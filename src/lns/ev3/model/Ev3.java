package lns.ev3.model;

import lejos.hardware.motor.Motor;

public class Ev3 {
	private int xp = 0;
	private int yp = 0;
	private int speed;
	private int direction = 0; //0==straight, -1==left, 1==right
	
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
		this.speed = speed;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public void ev3(int xp, int yp) {
		xp = this.xp;
		yp = this.yp;

		Motor.B.setSpeed(100);
		Motor.C.setSpeed(100);
	}

	public void straightOneBlock() { //Y좌표로 한블럭 직선 이동
		Motor.B.rotate(getOneBlockDist(), true);
		Motor.C.rotate(getOneBlockDist());
	}

	public void turnRight() { //오른쪽으로 회전
		int degree = 90;
		if (speed == 0 || direction==1)
			return;
		Motor.B.rotate(degree * 2, true);
		Motor.C.rotate(-degree * 2);
		direction++;
	}

	public void turnLeft() { //왼쪽으로 회전
		int degree = 90;
		if (speed == 0 || direction==-1)
			return;		
		Motor.B.rotate(-degree * 2, true);
		Motor.C.rotate(degree * 2);

		direction--;
	}

	public int getOneBlockDist() {
		return 360*speed/100;
	}
}
