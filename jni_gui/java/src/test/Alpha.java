package test;

import static org.mini.gl.GL.GL_AMBIENT;
import static org.mini.gl.GL.GL_AMBIENT_AND_DIFFUSE;
import static org.mini.gl.GL.GL_BLEND;
import static org.mini.gl.GL.GL_COLOR_BUFFER_BIT;
import static org.mini.gl.GL.GL_DEPTH_BUFFER_BIT;
import static org.mini.gl.GL.GL_DEPTH_TEST;
import static org.mini.gl.GL.GL_DIFFUSE;
import static org.mini.gl.GL.GL_EMISSION;
import static org.mini.gl.GL.GL_FALSE;
import static org.mini.gl.GL.GL_FRONT;
import static org.mini.gl.GL.GL_LIGHT0;
import static org.mini.gl.GL.GL_LIGHTING;
import static org.mini.gl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static org.mini.gl.GL.GL_POSITION;
import static org.mini.gl.GL.GL_SHININESS;
import static org.mini.gl.GL.GL_SPECULAR;
import static org.mini.gl.GL.GL_SRC_ALPHA;
import static org.mini.gl.GL.GL_TRUE;
import static org.mini.gl.GL.glBlendFunc;
import static org.mini.gl.GL.glClear;
import static org.mini.gl.GL.glDepthMask;
import static org.mini.gl.GL.glEnable;
import static org.mini.gl.GL.glLightfv;
import static org.mini.gl.GL.glMaterialf;
import static org.mini.gl.GL.glMaterialfv;
import static org.mini.gl.GL.glPopMatrix;
import static org.mini.gl.GL.glPushMatrix;
import static org.mini.gl.GL.glTranslatef;
import org.mini.glfw.Glfw;
import org.mini.glfw.GlfwCallbackAdapter;
import org.mini.glfw.utils.Gutil;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author gust
 */
public class Alpha {

    boolean exit = false;
    long curWin;
    int mx, my;

    class CallBack extends GlfwCallbackAdapter {

        @Override
        public void key(long window, int key, int scancode, int action, int mods) {
            System.out.println("key:" + key + " action:" + action);
            if (key == Glfw.GLFW_KEY_ESCAPE && action == Glfw.GLFW_PRESS) {
                Glfw.glfwSetWindowShouldClose(window, Glfw.GLFW_TRUE);
            }
        }

        @Override
        public void mouseButton(long window, int button, boolean pressed) {
            if (window == curWin) {
                String bt = button == Glfw.GLFW_MOUSE_BUTTON_LEFT ? "LEFT" : button == Glfw.GLFW_MOUSE_BUTTON_2 ? "RIGHT" : "OTHER";
                String press = pressed ? "pressed" : "released";
                System.out.println(bt + " " + mx + " " + my + "  " + press);
            }
        }

        @Override
        public void cursorPos(long window, int x, int y) {
            curWin = window;
            mx = x;
            my = y;
        }

        @Override
        public boolean windowClose(long window) {
            System.out.println("byebye");
            return true;
        }

        @Override
        public void windowSize(long window, int width, int height) {
            System.out.println("resize " + width + " " + height);
        }

        @Override
        public void framebufferSize(long window, int x, int y) {
        }
    }

   
    int w, h;

    void t1() {
        Glfw.glfwInit();
//        Glfw.glfwWindowHint(Glfw.GLFW_CONTEXT_VERSION_MAJOR, 3);
//        Glfw.glfwWindowHint(Glfw.GLFW_CONTEXT_VERSION_MINOR, 3);
//        Glfw.glfwWindowHint(Glfw.GLFW_DEPTH_BITS, 16);
//        Glfw.glfwWindowHint(Glfw.GLFW_TRANSPARENT_FRAMEBUFFER, Glfw.GLFW_TRUE);
        long win = Glfw.glfwCreateWindow(300, 300, "hello glfw", 0, 0);
        if (win != 0) {
            Glfw.glfwSetCallback(win, new CallBack());
            Glfw.glfwMakeContextCurrent(win);
//            Glfw.glfwSwapInterval(1);

            w = Glfw.glfwGetFramebufferSizeW(win);
            h = Glfw.glfwGetFramebufferSizeH(win);
            System.out.println("w=" + w + "  ,h=" + h);

            long last = System.currentTimeMillis(), now;
            int count = 0;
            while (!Glfw.glfwWindowShouldClose(win)) {
                int sleep = 100;

                draw2();
                Gutil.drawCood();

                Glfw.glfwPollEvents();
                Glfw.glfwSwapBuffers(win);

                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                }
                count++;
                now = System.currentTimeMillis();
                if (now - last > 1000) {
                    System.out.println("fps:" + count);
                    last = now;
                    count = 0;
                }
            }
            Glfw.glfwTerminate();
        }
    }

    public static void main(String[] args) {
        Alpha gt = new Alpha();
        gt.t1();

    }
    //===========================================================
    //===========================================================
    //===========================================================

    static float light_position[] = {1.0f, 1.0f, -1.0f, 1.0f};
    static float light_ambient[] = {0.2f, 0.2f, 0.2f, 1.0f};
    static float light_diffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};
    static float light_specular[] = {1.0f, 1.0f, 1.0f, 1.0f};

    void setLight() {

        glLightfv(GL_LIGHT0, GL_POSITION, light_position, 0);
        glLightfv(GL_LIGHT0, GL_AMBIENT, light_ambient, 0);
        glLightfv(GL_LIGHT0, GL_DIFFUSE, light_diffuse, 0);
        glLightfv(GL_LIGHT0, GL_SPECULAR, light_specular, 0);

        glEnable(GL_LIGHT0);
        glEnable(GL_LIGHTING);
        glEnable(GL_DEPTH_TEST);
    }
    static float mat_specular[] = {0.0f, 0.0f, 0.0f, 1.0f};
    static float mat_emission[] = {0.0f, 0.0f, 0.0f, 1.0f};

    void setMatirial(float[] mat_diffuse, float mat_shininess) {

        glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, mat_diffuse, 0);
        glMaterialfv(GL_FRONT, GL_SPECULAR, mat_specular, 0);
        glMaterialfv(GL_FRONT, GL_EMISSION, mat_emission, 0);
        glMaterialf(GL_FRONT, GL_SHININESS, mat_shininess);
    }

    static float red_color[] = {1.0f, 0.0f, 0.0f, 1.0f};
    static float green_color[] = {0.0f, 1.0f, 0.0f, 0.3333f};
    static float blue_color[] = {0.0f, 0.0f, 1.0f, 0.5f};

    Ball red = new Ball(0.3f, 8, Ball.SOLID);
    Ball blue = new Ball(0.2f, 8, Ball.SOLID);
    Ball green = new Ball(0.15f, 8, Ball.SOLID);

    void draw2() {
        // 定义一些材质颜色

        // 清除屏幕
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // 启动混合并设置混合因子
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // 设置光源
        setLight();

        // 以(0, 0, 0.5)为中心，绘制一个半径为.3的不透明红色球体（离观察者最远）
        setMatirial(red_color, 30.0f);
        glPushMatrix();
        glTranslatef(0.0f, 0.0f, 0.5f);
//    glutSolidSphere(0.3, 30, 30);
        red.drawSphere();
        glPopMatrix();

        // 下面将绘制半透明物体了，因此将深度缓冲设置为只读
        glDepthMask(GL_FALSE);

        // 以(0.2, 0, -0.5)为中心，绘制一个半径为.2的半透明蓝色球体（离观察者最近）
        setMatirial(blue_color, 30.0f);
        glPushMatrix();
        glTranslatef(0.2f, 0.0f, -0.5f);
//    glutSolidSphere(0.2, 30, 30);
        blue.drawSphere();
        glPopMatrix();

        // 以(0.1, 0, 0)为中心，绘制一个半径为.15的半透明绿色球体（在前两个球体之间）
        setMatirial(green_color, 30.0f);
        glPushMatrix();
        glTranslatef(0.1f, 0, 0);
//    glutSolidSphere(0.15, 30, 30);
        green.drawSphere();
        glPopMatrix();

        // 完成半透明物体的绘制，将深度缓冲区恢复为可读可写的形式
        glDepthMask(GL_TRUE);

    }

}
