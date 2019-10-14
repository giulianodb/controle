var dragObject;
var lastMouseX = 0;
var lastMouseY = 0;
var lastPosX;
var lastPosY;
function dragMouseMoveEvent(event) {
	event = event || window.event;
	
	var x = event.pageX;
	var y = event.pageY;
	var dx = x - lastMouseX;
	var dy = y - lastMouseY;
	lastMouseX = x;
	lastMouseY = y;
	
	if(dragObject){
		moveObject(dragObject, dx, dy);
	}
}

function moveObject(obj, dx, dy){
	var style = obj.style;
	lastPosX += dx;
	lastPosY += dy;
	style.top = (lastPosY)+"px";
	style.left = (lastPosX)+"px";
}

function findPos(obj) {
	var other = obj;
	var left = other.offsetLeft;
	var top = other.offsetTop;
	while (other = other.offsetParent) {
		left += other.offsetLeft;
		top += other.offsetTop;
	}
	lastPosX = left;
	lastPosY = top;
	moveObject(obj, 0, 0);
	obj.style.marginTop = '0px';
	obj.style.marginLeft = '0px';
}

function startDrag(obj) {
	findPos(obj);
	dragObject = obj;
	dragObject.style.cursor = "move";
}

function stopDrag() {
	if(dragObject){
		dragObject.style.cursor = "default";
		dragObject = null;
	}
}

function maximizar(obj){
	var style = obj.style;
	style.top = '0px';
	style.left = '0px';
	style.width = '100%';
	style.height = '100%';
	style.zIndex = 50000;
}

//
/*
function fechar() { document.getElementById(id).style.visibility = "hidden"; }
function abrir() { document.getElementById(id).style.visibility = "visible"; }
*/
//
/*
function minimax() {
	var obj = document.getElementById("conteudo");
	var btn = document.getElementById("minimax");
	if (!mini) {
		obj.style.display = "none";
		btn.innerHTML = "+";
		btn.setAttribute("title", "Maximizar");
		mini = true;
	} else {
		obj.style.display = "block";
		btn.innerHTML = "-";
		btn.setAttribute("title", "Minimizar");
		document.getElementById("statusbar").style.display = "block";
		mini = false;
	}
}
*/
//
/*
function esconder() {
	if (!mini) {
		minimax();
	}
	document.getElementById(id).style.top = 0;
	document.getElementById(id).style.left = 0;
	document.getElementById("statusbar").style.display = "none";
}
*/

document.onmousemove = function(event) { dragMouseMoveEvent(event); }