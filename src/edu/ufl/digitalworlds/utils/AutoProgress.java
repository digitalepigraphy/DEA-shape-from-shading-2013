package edu.ufl.digitalworlds.utils;

public class AutoProgress extends ParallelThread{

	double duration;
	double factor;
	int progress;
	
	public AutoProgress(int start_duration, double progress_factor)
	{
		duration=start_duration;
		factor=progress_factor;
	}
	
	@Override
	public void run() {
		int value=0;
		if(progress_listener!=null)
		{
			progress_listener.setMaxProgress(100);
			progress_listener.setProgress(value);
		}
		while(thread!=null && value<100)
		{
			duration*=factor;
			if(progress_listener!=null && thread!=null){value+=1;progress_listener.setProgress(value);}
			try {Thread.sleep((int)duration);} catch (InterruptedException e) {}
		}
		thread=null;
	}
}
