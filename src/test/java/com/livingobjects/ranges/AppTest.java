package com.livingobjects.ranges;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class AppTest
{

	@Test
    public void testApp()
    {
		String[] args = { "appName", "-junit" };
		App.main(args);
    }
}
