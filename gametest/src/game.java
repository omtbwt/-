import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;

public class game {
    public static void main(String[] ar) {
        game_Frame fms = new game_Frame();
        fms.MessengerMulti();
    }
}

class game_Frame extends JFrame implements KeyListener, Runnable {

    final static int ServerPort = 5002;
    DataInputStream is;
    DataOutputStream os;
    int num; //p1 p2 구분
    
    int f_width = 800;
    int f_height = 600;

    int x_p1, y_p1;
    int x_p2, y_p2;
    
    boolean KeyUp = false;
    boolean KeyDown = false;
    boolean KeyLeft = false;
    boolean KeyRight = false;
    boolean KeySpace = false; //미사일 발사

    Toolkit tk = Toolkit.getDefaultToolkit();
    //p1
    Image me_img = tk.getImage("image/f15k_0.png");
    Image buffImage;
    Graphics buffg;
    //p2
    Image me_img_p2 = tk.getImage("image/f15k_0p2.png");
    
    Image Missile_img = tk.getImage("image/Missile.png"); //미사일 이미지 변수
    ArrayList Missile_List_p1 = new ArrayList();
    ArrayList Missile_List_p2 = new ArrayList();
    
    Missile ms; // 미사일 클래스 접근 키

    
    Thread th;

    game_Frame() {
        init();
        start();

        setTitle("슈팅 게임 만들기");
        setSize(f_width, f_height);
        Dimension screen = tk.getScreenSize();

        int f_xpos = (int) (screen.getWidth() / 2 - f_width / 2);
        int f_ypos = (int) (screen.getHeight() / 2 - f_height / 2);

        setLocation(f_xpos, f_ypos);
        setResizable(false);
        setVisible(true);
        addKeyListener(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    class Missile{ // 미사일 위치 파악 및 이동을 위한 클래스 추가 

    	Point pos; //미사일 좌표 변수
    	Missile(int x, int y){ //미사일 좌표를 입력 받는 메소드
    		pos = new Point(x, y); //미사일 좌표를 체크
    	}
    	public void move(){ //미사일 이동을 위한 메소드
    		if(num==0)
    			pos.x += 10; //x 좌표에 10만큼 미사일 이동 p1
    		else if (num==1)
    			pos.x -= 10; //x 좌표에 -10만큼 미사일 이동 p2
    	}
    }
    
    public void init() { 

    	x_p1 = 100;
    	y_p1 = 100;

    	x_p2 = 500;
    	y_p2 = 100;

    }

    public void start() {
        th = new Thread(this);
        th.start();
    }

    public void run() {
        try {
            while (true) {
                KeyProcess();
                MissileProcess();
                repaint();
                Thread.sleep(20);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void paint(Graphics g) {
        buffImage = createImage(f_width, f_height);
        buffg = buffImage.getGraphics();
        update(g);
    }

    public void update(Graphics g) {
        Draw_Char();
        Draw_Missile();
        g.drawImage(buffImage, 0, 0, this);

    }

    public void MissileProcess() {
        if (KeySpace) {
	            // 스페이스바 키가 눌렸을 때만 미사일 객체 생성
	            if(num==0) {
		        	ms = new Missile(x_p1, y_p1);
		            try {
		                os.writeUTF("M_p1"); // p1의 미사일 발사 알리기
		            } catch (IOException ex) {
		                ex.printStackTrace();
		            }
		            Missile_List_p1.add(ms); // 해당 미사일 추가
		            KeySpace = false; // 발사 후 키 상태 초기화
	            } 
	            if(num==1) {
		        	ms = new Missile(x_p2, y_p2);
		            try {
		                os.writeUTF("M_p2"); // p1의 미사일 발사 알리기
		            } catch (IOException ex) {
		                ex.printStackTrace();
		            }
		            Missile_List_p2.add(ms); // 해당 미사일 추가
		            KeySpace = false; // 발사 후 키 상태 초기화
		        }
        }
    }

    
    public void Draw_Char() {
        buffg.clearRect(0, 0, f_width, f_height);
        buffg.drawImage(me_img, x_p1, y_p1, this);
        buffg.drawImage(me_img_p2, x_p2, y_p2, this);
    }

    public void keyPressed(KeyEvent e) {
        try {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    KeyUp = true;
                    break;
                case KeyEvent.VK_DOWN:
                    KeyDown = true;
                    break;
                case KeyEvent.VK_LEFT:
                    KeyLeft = true;
                    break;
                case KeyEvent.VK_RIGHT:
                    KeyRight = true;
                    break;
                case KeyEvent.VK_SPACE : // 스페이스키 입력 처리 추가
                	KeySpace = true;
                	break;
            }
            os.writeUTF("X_p1");
            os.writeInt(x_p1);
            os.writeUTF("Y_p1");
            os.writeInt(y_p1);
            os.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    
    public void Draw_Missile(){ // 미사일 그리는 메소드
    	if(num==0) {//p1
	    	for (int i = 0 ; i < Missile_List_p1.size()  ; ++i){
	    	//미사일 존재 유무를 확인한다.
	
		    	ms = (Missile) (Missile_List_p1.get(i)); 
		    	// 미사일 위치값을 확인
		
		    	buffg.drawImage(Missile_img, ms.pos.x + 150, ms.pos.y + 30, this); 
		    	// 현재 좌표에 미사일 그리기.
		    	// 이미지 크기를 감안한 미사일 발사 좌표는 수정됨.
		
		    	ms.move();
		    	// 그려진 미사일을 정해진 숫자만큼 이동시키기
		
		    	if ( ms.pos.x > f_width || ms.pos.x < 0 ){ // 미사일이 화면 밖으로 나가면
	    			Missile_List_p1.remove(i); // 미사일 지우기
	    		}
	    	}
    	}
    	else if(num==1) {//p2
    		for (int i = 0 ; i < Missile_List_p2.size()  ; ++i){
    	    	//미사일 존재 유무를 확인한다.
    	
    		    	ms = (Missile) (Missile_List_p2.get(i)); 
    		    	// 미사일 위치값을 확인
    		
    		    	buffg.drawImage(Missile_img, ms.pos.x, ms.pos.y + 30, this); 
    		    	// 현재 좌표에 미사일 그리기.
    		    	// 이미지 크기를 감안한 미사일 발사 좌표는 수정됨.
    		
    		    	ms.move();
    		    	// 그려진 미사일을 정해진 숫자만큼 이동시키기
    		
    		    	if ( ms.pos.x > f_width ){ // 미사일이 화면 밖으로 나가면
    	    			Missile_List_p2.remove(i); // 미사일 지우기
    	    		}
    	    	}
    	}
    }
    
    public void keyReleased(KeyEvent e) {
        
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    KeyUp = false;
                    break;
                case KeyEvent.VK_DOWN:
                    KeyDown = false;
                    break;
                case KeyEvent.VK_LEFT:
                    KeyLeft = false;
                    break;
                case KeyEvent.VK_RIGHT:
                    KeyRight = false;
                    break;
                case KeyEvent.VK_SPACE : // 스페이스키 입력 처리 추가
                	KeySpace = false;
                	break;
            }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void KeyProcess() {
        if(num==0) {//p1일때
	    	int newX = x_p1;
	        int newY = y_p1;
	
	        if (KeyUp) newY -= 5;
	        if (KeyDown) newY += 5;
	        if (KeyLeft) newX -= 5;
	        if (KeyRight) newX += 5;
	
	        // 값이 변경되었을 때만 전송
	        if (newX != x_p1 || newY != y_p1) {
	            try {
	                os.writeUTF("X_p1");
	                os.writeInt(newX);
	                os.writeUTF("Y_p1");
	                os.writeInt(newY);
	                os.flush();
	                
	                x_p1 = newX;
	                y_p1 = newY;
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
        }
        else if(num==1) {//p2일때
        	int newX = x_p2;
	        int newY = y_p2;
	
	        if (KeyUp) newY -= 5;
	        if (KeyDown) newY += 5;
	        if (KeyLeft) newX -= 5;
	        if (KeyRight) newX += 5;
	
	        // 값이 변경되었을 때만 전송
	        if (newX != x_p2 || newY != y_p2) {
	            try {
	                os.writeUTF("X_p2");
	                os.writeInt(newX);
	                os.writeUTF("Y_p2");
	                os.writeInt(newY);
	                os.flush();
	                
	                x_p2 = newX;
	                y_p2 = newY;
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
        }
    }

    public void MessengerMulti() {
        try {
            InetAddress ip = InetAddress.getByName("localhost");
            Socket s = new Socket(ip, ServerPort);
            is = new DataInputStream(s.getInputStream());
            os = new DataOutputStream(s.getOutputStream());
            
            try {
            	num = is.readInt();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            
            Thread thread2 = new Thread(() -> {
                while (true) {
                    try {
                        String messageType = is.readUTF();

                        if (messageType.equals("X_p1")) {
                            // 정수값 x 수신
                            x_p1 = is.readInt();
                        } 
                        else if (messageType.equals("Y_p1")) {
                            // 정수값 y 수신
                            y_p1 = is.readInt();
                        }
                        else if (messageType.equals("X_p2")) {
                            // 정수값 y 수신
                            x_p2 = is.readInt();
                        }
                        else if (messageType.equals("Y_p2")) {
                            // 정수값 y 수신
                            y_p2 = is.readInt();
                        }
                        else if (messageType.equals("M_p1")) {
                        	ms = new Missile(x_p1, y_p1);
                        	Missile_List_p1.add(ms);
                        }
                        else if (messageType.equals("M_p2")) {
                        	ms = new Missile(x_p2, y_p2);
                        	Missile_List_p2.add(ms);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
