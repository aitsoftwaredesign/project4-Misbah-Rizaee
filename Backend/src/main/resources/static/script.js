function scrollAppear() {
  let left = document.querySelector('#left');
  let right = document.querySelector('#right');
  let position = left.getBoundingClientRect().top;
  let screenPosition = window.innerHeight / 1.5;
  
  if(position < screenPosition) {
    left.classList.add('showLeft');
    right.classList.add('showRight');
  }
	else {
		left.classList.remove('showLeft');
		right.classList.remove('showRight');
	}
}

function openNav() {
	document.getElementById("mySidenav").style.width = "100%";
}

function closeNav() {
	document.getElementById("mySidenav").style.width = "0";
}

function scrollColor() {
	if (document.body.scrollTop > 80 || document.documentElement.scrollTop > 80) {
		document.getElementById("myTopnav").style.backgroundColor = "#17252a";
		document.getElementById("myTopnav").style.transition = "0.4s";
	} 
	else {
		document.getElementById("myTopnav").style.backgroundColor = "transparent";
		document.getElementById("myTopnav").style.transition = "0.4s";
	}
}

window.onload = function(){ 
window.addEventListener('scroll', scrollAppear);
window.addEventListener('scroll', scrollColor);
document.getElementById("mySidenav").onclick = function() {openNav()};
document.getElementById("mySidenav").onclick = function() {closeNav()};
};