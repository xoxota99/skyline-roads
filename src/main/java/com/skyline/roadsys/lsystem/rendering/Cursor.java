package com.skyline.roadsys.lsystem.rendering;

import javax.vecmath.*;

import com.skyline.roadsys.geometry.*;
import com.skyline.roadsys.util.*;
/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

public class Cursor {
	private Point position;
	private Vector3d direction;

	public Cursor() {
		position = new Point(0, 0, 0);
		direction = new Vector3d(0, 0, 0);
	}

	public Cursor(Point position, Vector3d direction) {
		this.position = position;
		this.direction = direction;
	}

	public Cursor(Cursor source) {
		this.position = new Point(source.getPosition());
		this.direction = new Vector3d(source.getDirection());
	}

	public void move(double distance) {
		direction.normalize();
		position.x = (position.x + direction.x * distance);
		position.y = (position.y + direction.y * distance);
		position.z = (position.z + direction.z * distance);
	}

	public void turn(double angle) {
		MathUtil.rotateAroundZ(direction, angle);
		direction.normalize();
	}

	public Point getPosition() {
		return position;
	}

	public void setPosition(Point position) {
		this.position = position;
	}

	public Vector3d getDirection() {
		return direction;
	}

	public void setDirection(Vector3d direction) {
		this.direction = direction;
	}

}
