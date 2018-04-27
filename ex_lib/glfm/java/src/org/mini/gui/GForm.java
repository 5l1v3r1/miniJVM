/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mini.gui;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import static org.mini.gl.GL.GL_COLOR_BUFFER_BIT;
import static org.mini.gl.GL.GL_DEPTH_BUFFER_BIT;
import static org.mini.gl.GL.GL_STENCIL_BUFFER_BIT;
import org.mini.nanovg.StbFont;
import static org.mini.gl.GL.glClear;
import static org.mini.gl.GL.glClearColor;
import static org.mini.gl.GL.glViewport;
import org.mini.glfm.Glfm;
import org.mini.glfm.GlfmCallBack;
import org.mini.glfm.GlfmCallBackAdapter;
import static org.mini.gui.GToolkit.nvgRGBA;
import org.mini.nanovg.Gutil;
import org.mini.nanovg.Nanovg;
import static org.mini.nanovg.Nanovg.NVG_ALIGN_MIDDLE;
import static org.mini.nanovg.Nanovg.NVG_ANTIALIAS;
import static org.mini.nanovg.Nanovg.NVG_DEBUG;
import static org.mini.nanovg.Nanovg.NVG_STENCIL_STROKES;
import static org.mini.nanovg.Nanovg.nvgBeginFrame;
import static org.mini.nanovg.Nanovg.nvgEndFrame;
import static org.mini.nanovg.Nanovg.nvgFillColor;
import static org.mini.nanovg.Nanovg.nvgFontFace;
import static org.mini.nanovg.Nanovg.nvgFontSize;
import static org.mini.nanovg.Nanovg.nvgTextAlign;

/**
 *
 * @author gust
 */
public class GForm extends GPanel {

    String title;
    int width;
    int height;
    long win; //glfw win
    long vg; //nk contex
    GlfmCallBack callback;
    static StbFont gfont;
    float fps;
    float fpsExpect = 30;

    //
    float pxRatio;
    int winWidth, winHeight;
    int fbWidth, fbHeight;
    //
    boolean premult;

    Timer timer = new Timer();//用于更新画面，UI系统采取按需刷新的原则

    public GForm(String title, int width, int height, long display) {
        this.title = title;
        this.width = width;
        this.height = height;
        boundle[WIDTH] = width;
        boundle[HEIGHT] = height;
        win = display;

        setCallBack(new FormCallBack());
    }

    public void setCallBack(GlfmCallBack callback) {
        this.callback = callback;
    }

    public GlfmCallBack getCallBack() {
        return this.callback;
    }

    static public void setGFont(StbFont pgfont) {
        gfont = pgfont;
    }

    static public StbFont getGFont() {
        return gfont;
    }

    public long getNvContext() {
        return vg;
    }

    public long getWinContext() {
        return win;
    }

    public int getDeviceWidth() {
        return (int) winWidth;
    }

    public int getDeviceHeight() {
        return (int) winHeight;
    }

    @Override
    void init() {

        vg = Nanovg.nvgCreateGLES2(NVG_ANTIALIAS | NVG_STENCIL_STROKES | NVG_DEBUG);
        if (vg == 0) {
            System.out.println("Could not init nanovg.\n");

        }
        String respath = Glfm.glfmGetResRoot();
        System.setProperty("word_font_path", respath + "/wqymhei.ttc");
        System.setProperty("icon_font_path", respath + "/entypo.ttf");
        System.setProperty("emoji_font_path", respath + "/NotoEmoji-Regular.ttf");
        GToolkit.loadFont(vg);

        fbWidth = Glfm.glfmGetDisplayWidth(win);
        fbHeight = Glfm.glfmGetDisplayHeight(win);
        // Calculate pixel ration for hi-dpi devices.
        pxRatio = (float) Glfm.glfmGetDisplayScale(win);
        winWidth = (int) (fbWidth / pxRatio);
        winHeight = (int) (fbHeight / pxRatio);
        boundle[WIDTH] = width;
        boundle[HEIGHT] = height;

        extinit.onInit(this);
        flush();
    }

    void display(long vg) {

        // Update and render
        glViewport(0, 0, fbWidth, fbHeight);
        if (premult) {
            glClearColor(0, 0, 0, 0);
        } else {
            glClearColor(0.3f, 0.3f, 0.32f, 1.0f);
        }
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        nvgBeginFrame(vg, winWidth, winHeight, pxRatio);
        drawDebugInfo(vg);
        update(vg);
        nvgEndFrame(vg);
    }

    void drawDebugInfo(long vg) {
        float font_size = 15;
        nvgFontSize(vg, font_size);
        nvgFontFace(vg, GToolkit.getFontWord());
        nvgTextAlign(vg, Nanovg.NVG_ALIGN_LEFT | NVG_ALIGN_MIDDLE);

        FormCallBack cb = (FormCallBack) callback;
        float dx = 10, dy = 10;
        byte[] b;
        nvgFillColor(vg, nvgRGBA(255, 255, 255, 255));

        b = Gutil.toUtf8("touch x:" + cb.mouseX);
        Nanovg.nvgTextJni(vg, dx, dy, b, 0, b.length);
        dy += font_size;
        b = Gutil.toUtf8("touch y:" + cb.mouseY);
        Nanovg.nvgTextJni(vg, dx, dy, b, 0, b.length);
        dy += font_size;
        if (focus != null) {
            b = Gutil.toUtf8("focus x:" + focus.boundle[LEFT] + " y:" + focus.boundle[TOP] + " w:" + focus.boundle[WIDTH] + " h:" + focus.boundle[HEIGHT]);
            Nanovg.nvgTextJni(vg, dx, dy, b, 0, b.length);
        }
    }

    void findSetFocus(int x, int y) {
        for (Iterator<GObject> it = elements.iterator(); it.hasNext();) {
            GObject nko = it.next();
            if (isInBoundle(nko.getBoundle(), x, y)) {
                setFocus(nko);
                break;
            }
        }
    }

    class FormCallBack extends GlfmCallBackAdapter {

        int mouseX, mouseY, lastX, lastY;
        long mouseLastPressed;
        int LONG_TOUCH_TIME = 1000;
        int INERTIA_MIN_DISTANCE = 40;//移动距离超过40单位时可以产生惯性
        int INERTIA_MAX_MILLS = 300;//在300毫秒内的滑动可以产生惯性

        long last, count;
        //
        double moveStartX;
        double moveStartY;
        long moveStartAt;

        @Override
        public void mainLoop(long display, double frameTime) {
            long startAt, endAt, cost;
            try {
                startAt = System.currentTimeMillis();
                if (flush) {
                    display(vg);
                    flush = false;
                }
                count++;
                endAt = System.currentTimeMillis();
                cost = endAt - startAt;
                if (cost > 1000) {
                    //System.out.println("fps:" + count);
                    fps = count;
                    last = endAt;
                    count = 0;
                }
//                if (cost < 1000 / fpsExpect) {
//                    Thread.sleep((long) (1000 / fpsExpect - cost));
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceCreated(long display, int width, int height) {
            GForm.this.init();
            flush();
        }

        @Override
        public boolean onKey(long display, int keyCode, int action, int modifiers) {
            //System.out.println("keyCode  :" + keyCode + "   action=" + action + "   modifiers=" + modifiers);
            if (focus != null) {
                focus.keyEvent(keyCode, action, modifiers);
            } else {
                GForm.this.keyEvent(keyCode, action, modifiers);
            }
            flush();
            return true;
        }

        @Override
        public void onCharacter(long window, String str, int modifiers) {
            //System.out.println("onCharacter  :" + str + "   mod=" + modifiers);
            if (focus != null) {
                focus.characterEvent(str, modifiers);
            } else {
                GForm.this.characterEvent(str, modifiers);
            }

            flush();
        }

        @Override
        public boolean onTouch(long display, int touch, int phase, double x, double y) {
            x /= Glfm.glfmGetDisplayScale(display);
            y /= Glfm.glfmGetDisplayScale(display);
            lastX = mouseX;
            lastY = mouseY;
            mouseX = (int) x;
            mouseY = (int) y;
//            System.out.println("   touch=" + touch + "   phase=" + phase + "   x=" + x + "   y=" + y);
//            System.out.println("display=" + display + "   win=" + win);
            if (display == win) {

                switch (phase) {
                    case Glfm.GLFMTouchPhaseBegan: {//
                        findSetFocus(mouseX, mouseY);//找到焦点组件

                        //处理惯性
                        moveStartX = x;
                        moveStartY = y;
                        moveStartAt = System.currentTimeMillis();
                        break;
                    }
                    case Glfm.GLFMTouchPhaseEnded: {//

                        long cost = System.currentTimeMillis() - moveStartAt;
                        if ((Math.abs(x - moveStartX) > INERTIA_MIN_DISTANCE || Math.abs(y - moveStartY) > INERTIA_MIN_DISTANCE)
                                && cost < INERTIA_MAX_MILLS) {//在短时间内进行了滑动操作
                            GForm.this.inertiaEvent(moveStartX, moveStartY, x, y, cost);
                        }

                        //处理惯性
                        moveStartX = 0;
                        moveStartY = 0;
                        moveStartAt = 0;
                        break;
                    }
                    case Glfm.GLFMTouchPhaseMoved: {//
                        if (focus != null) {
                            focus.scrollEvent(mouseX - lastX, mouseY - lastY, mouseX, mouseY);
                        } else {
                            GForm.this.scrollEvent(mouseX - lastX, mouseY - lastY, mouseX, mouseY);
                        }
                        break;
                    }
                    case Glfm.GLFMTouchPhaseHover: {//
                        break;
                    }
                }
                long cur = System.currentTimeMillis();
                //双击
                boolean long_touched = cur - mouseLastPressed > LONG_TOUCH_TIME;
                if (phase == Glfm.GLFMTouchPhaseBegan) {
                    mouseLastPressed = cur;
                }

                //click event
                if (long_touched) {
                    if (focus != null) {
                        focus.longTouchedEvent(mouseX, mouseY);
                    } else {
                        GForm.this.longTouchedEvent(mouseX, mouseY);
                    }
                    long_touched = false;
                }
                if (focus != null) {//press event
                    focus.touchEvent(phase, mouseX, mouseY);
                } else {
                    GForm.this.touchEvent(phase, mouseX, mouseY);
                }
            }
            flush();
            return true;
        }

        @Override
        public void onSurfaceDestroyed(long window) {

        }

        @Override
        public void onSurfaceResize(long window, int width, int height) {
            GForm.this.boundle[WIDTH] = width;
            GForm.this.boundle[HEIGHT] = height;
            flush();
        }

        public void onSurfaceError(long display, String description) {
            //System.out.println("error message: " + description);
            flush();
        }
    }

    TimerTask tt_OnTouch = new TimerTask() {
        public void run() {
            flush = true;
        }
    };

    void tt_setupOnTouch() {
        timer.schedule(tt_OnTouch, 0L);//, (long) (1000 / fpsExpect));
    }

    /**
     * @return the fps
     */
    public float getFps() {
        return fps;
    }

    public void setFps(float fps) {
        fpsExpect = fps;
    }

}
