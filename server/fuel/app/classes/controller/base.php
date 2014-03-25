<?php
use Fuel\Core\Controller_Template;

class Controller_Base extends Controller_Template {
	public $data = array();
	public $view = '';
	
	public function before() {
		parent::before();
	}
	
	public function test() {
	}
	
	public function after($response) {
		if(!empty($this->view)) {
			$this->template->content = View::forge($this->view, $this->data);
		}
		return parent::after($response);
	}
	
}