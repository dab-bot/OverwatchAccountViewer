package controllers;

import application.Main;

import java.sql.Connection;

public abstract class Controller {

	public abstract void setMain(Main main);

	public abstract void setConnection(Connection conDB);

	public abstract void fill();
}
