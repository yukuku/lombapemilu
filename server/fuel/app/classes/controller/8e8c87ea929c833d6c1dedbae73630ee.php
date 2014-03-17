<?php
class Controller_Admin extends Controller_Base {
	public function before() {
		parent::before();
		Casset::css('admin.css');
		Casset::js('bootstrap.js');
		Casset::js('admin.js');
	}
	
	public function action_index() {
		$this->template->content = View::forge('admin/index');
	}
}