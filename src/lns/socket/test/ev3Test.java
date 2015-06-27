package lns.socket.test;

import lns.ev3.model.Ev3;

public class ev3Test {
	private static Ev3 slaveEv3;
	
	public static void main(String[] args) {
		slaveEv3 = new Ev3();
		slaveEv3.ev3SetUp(0, 4, 100, 3, 0);
		
		while ( !successMove() ) {
			slaveEv3.straightOneBlock();
			if( slaveEv3.getDirection() == 1)
				slaveEv3.turnLeft();
			else if ( slaveEv3.getDirection() == 0)
				slaveEv3.turnRight();
		}
	}

	private static boolean successMove() {
		if( slaveEv3.getXp() != slaveEv3.getTargetXp() )
				return false;
		else if ( slaveEv3.getYp() != slaveEv3.getTargetYp() )
			return false;
		
		return true;
	}
}
