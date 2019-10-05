function HLCamUtils(vidElementName) {
	var me 	= this;
	var vid = document.getElementById(vidElementName);
	
	this.grabFrameAsCanvas = function()
	{
		if(vid)
		{
			var canvas 		= document.createElement("canvas");
			var context 	= canvas.getContext('2d');
			canvas.height	= vid.height;
			canvas.width	= vid.width;
			context.drawImage(vid, 0, 0, canvas.width, canvas.height);
			return canvas;
		}
		return null;
	}
	
	this.grabFrameAsBase64 = function(imgFormat)
	{
		var c = me.grabFrameAsCanvas();
		if(c)
		{
			if(imgFormat==null)
			{
				imgFormat = "jpeg";
			}
			return c.toDataURL("image/"+imgFormat);
		}
		return null;
	}
	
	this.grabFrameAsImg = function(imgFormat)
	{
		var c = me.grabFrameAsCanvas();
		if(c)
		{
			if(imgFormat==null)
			{
				imgFormat = "jpeg";
			}
			var img = new Image();
			img.src = c.toDataURL("image/"+imgFormat);
			return img;
		}
		return null;
	}
	
	this.downloadBase64AsFile = function(filename) 
	{
		if(filename==null)
		{
			filename = "snapshot-jpg.base64";
		}
		var e = document.createElement('a');
		e.setAttribute('href', 'data:image/jpeg:base64,'+me.getSnapshotBase64());
		e.setAttribute('download', filename);
		e.style.display = 'none';
		document.body.appendChild(e);
		e.click();
		document.body.removeChild(e);
	}
	
	this.pause = function()
	{
		vid.pause();
	}
	
	this.play = function()
	{
		vid.play();
	}
	
	this.initWebcam = function()
	{
		navigator.getUserMedia = navigator.getUserMedia 
			|| navigator.webkitGetUserMedia 
		    || navigator.mozGetUserMedia 
			|| navigator.msGetUserMedia 
			|| navigator.oGetUserMedia;
		
		if (navigator.getUserMedia) {       
			navigator.getUserMedia({video: true}, handleVideo, videoError);
		}
		
		function handleVideo(stream) {
			vid.srcObject = stream;
			/*vid.src = window.URL.createObjectURL(stream);*/
		}
		
		function videoError(e) {
			// do something
			console.log(e.name+" - "+e.message);
		}
	}
}
