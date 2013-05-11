class moremissgrant {
  static void main(String args[]) throws java.io.IOException { 
     new moremissgrant().run(new java.util.Scanner(System.in), 
                    new java.io.PrintWriter(System.out));
  }
  
  private static final int state$idle = 0;
  
  private static final int state$active = 1;
  
  private static final int state$waitingForLight = 2;
  
  private static final int state$waitingForBrick = 3;
  
  private static final int state$waitingForDrawer = 4;
  
  private static final int state$unlockedSafe = 5;
  
  void run(java.util.Scanner input, java.io.Writer output) throws java.io.IOException {
    int state = state$idle;
    while (true) {
      String token = input.nextLine();
      switch (state) {
        
        case state$idle: {
          
             unlockDoor(output);
          
             lockSafe(output);
          
          
          if (doorClosed(token)) {
             state = state$active;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          if (drapesOpened(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$active: {
          
             lockDoor(output);
          
          
          if (drapesClosed(token)) {
             state = state$waitingForLight;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          if (drapesOpened(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitingForLight: {
          
          
          if (lightOn(token)) {
             state = state$waitingForBrick;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          if (drapesOpened(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitingForBrick: {
          
          
          if (brickTouched(token)) {
             state = state$waitingForDrawer;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          if (drapesOpened(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer: {
          
          
          if (drawerOpened(token)) {
             state = state$unlockedSafe;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          if (drapesOpened(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$unlockedSafe: {
          
             unlockSafe(output);
          
          
          if (safeClosed(token)) {
             state = state$idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          if (drapesOpened(token)) {
             state = state$idle;
          }
          
          break;
        }
        
      }
    }
  }
  
  private boolean drapesClosed(String token) {
    return token.equals("DPCL");
  }
  
  private boolean drapesOpened(String token) {
    return token.equals("DPOP");
  }
  
  private boolean doorClosed(String token) {
    return token.equals("D1CL");
  }
  
  private boolean brickTouched(String token) {
    return token.equals("BRTC");
  }
  
  private boolean drawerOpened(String token) {
    return token.equals("D2OP");
  }
  
  private boolean lightOn(String token) {
    return token.equals("L1ON");
  }
  
  private boolean doorOpened(String token) {
    return token.equals("D1OP");
  }
  
  private boolean safeClosed(String token) {
    return token.equals("SFCL");
  }
  
  
  private void unlockSafe(java.io.Writer output) throws java.io.IOException {
    output.write("SFUN\n");
    output.flush();
  }
  
  private void lockSafe(java.io.Writer output) throws java.io.IOException {
    output.write("LKSF\n");
    output.flush();
  }
  
  private void lockDoor(java.io.Writer output) throws java.io.IOException {
    output.write("D1LK\n");
    output.flush();
  }
  
  private void unlockDoor(java.io.Writer output) throws java.io.IOException {
    output.write("D1UL\n");
    output.flush();
  }
  
}