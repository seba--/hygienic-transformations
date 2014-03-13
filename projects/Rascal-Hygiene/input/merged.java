public class merged {
  public static void main(String args[]) throws java.io.IOException { 
     new merged().run(new java.util.Scanner(System.in), 
                    new java.io.PrintWriter(System.out));
  }
  
  private static final int state$idle__idle = 0;
  
  private static final int state$active__idle = 1;
  
  private static final int state$waitingForDrawer__idle = 2;
  
  private static final int state$waitingForLight__idle = 3;
  
  private static final int state$unlockedPanel__idle = 4;
  
  private static final int state$unlockedPanel__waitingForRest = 5;
  
  private static final int state$idle__waitingForRest = 6;
  
  private static final int state$active__waitingForRest = 7;
  
  private static final int state$waitingForDrawer__waitingForRest = 8;
  
  private static final int state$waitingForLight__waitingForRest = 9;
  
  private static final int state$waitingForLight__waitFridgeCandleBook = 10;
  
  private static final int state$unlockedPanel__waitFridgeCandleBook = 11;
  
  private static final int state$idle__waitFridgeCandleBook = 12;
  
  private static final int state$active__waitFridgeCandleBook = 13;
  
  private static final int state$waitingForDrawer__waitFridgeCandleBook = 14;
  
  private static final int state$waitingForDrawer__waitFridgeCandle = 15;
  
  private static final int state$unlockedPanel__waitFridgeCandle = 16;
  
  private static final int state$idle__waitFridgeCandle = 17;
  
  private static final int state$active__waitFridgeCandle = 18;
  
  private static final int state$waitingForLight__waitFridgeCandle = 19;
  
  private static final int state$waitingForLight__waitFridgeFireBook = 20;
  
  private static final int state$unlockedPanel__waitFridgeFireBook = 21;
  
  private static final int state$idle__waitFridgeFireBook = 22;
  
  private static final int state$active__waitFridgeFireBook = 23;
  
  private static final int state$waitingForDrawer__waitFridgeFireBook = 24;
  
  private static final int state$waitingForDrawer__waitFridgeFire = 25;
  
  private static final int state$unlockedPanel__waitFridgeFire = 26;
  
  private static final int state$idle__waitFridgeFire = 27;
  
  private static final int state$active__waitFridgeFire = 28;
  
  private static final int state$waitingForLight__waitFridgeFire = 29;
  
  private static final int state$waitingForDrawer__waitFridgeBook = 30;
  
  private static final int state$unlockedPanel__waitFridgeBook = 31;
  
  private static final int state$idle__waitFridgeBook = 32;
  
  private static final int state$active__waitFridgeBook = 33;
  
  private static final int state$waitingForLight__waitFridgeBook = 34;
  
  private static final int state$waitingForLight__waitFridge = 35;
  
  private static final int state$unlockedPanel__waitFridge = 36;
  
  private static final int state$idle__waitFridge = 37;
  
  private static final int state$active__waitFridge = 38;
  
  private static final int state$waitingForDrawer__waitFridge = 39;
  
  private static final int state$waitingForLight__waitCandleFireBook = 40;
  
  private static final int state$unlockedPanel__waitCandleFireBook = 41;
  
  private static final int state$idle__waitCandleFireBook = 42;
  
  private static final int state$active__waitCandleFireBook = 43;
  
  private static final int state$waitingForDrawer__waitCandleFireBook = 44;
  
  private static final int state$waitingForDrawer__waitCandleFire = 45;
  
  private static final int state$unlockedPanel__waitCandleFire = 46;
  
  private static final int state$idle__waitCandleFire = 47;
  
  private static final int state$active__waitCandleFire = 48;
  
  private static final int state$waitingForLight__waitCandleFire = 49;
  
  private static final int state$waitingForDrawer__waitCandleBook = 50;
  
  private static final int state$unlockedPanel__waitCandleBook = 51;
  
  private static final int state$idle__waitCandleBook = 52;
  
  private static final int state$active__waitCandleBook = 53;
  
  private static final int state$waitingForLight__waitCandleBook = 54;
  
  private static final int state$waitingForLight__waitCandle = 55;
  
  private static final int state$unlockedPanel__waitCandle = 56;
  
  private static final int state$idle__waitCandle = 57;
  
  private static final int state$active__waitCandle = 58;
  
  private static final int state$waitingForDrawer__waitCandle = 59;
  
  private static final int state$waitingForDrawer__waitFireBook = 60;
  
  private static final int state$unlockedPanel__waitFireBook = 61;
  
  private static final int state$idle__waitFireBook = 62;
  
  private static final int state$active__waitFireBook = 63;
  
  private static final int state$waitingForLight__waitFireBook = 64;
  
  private static final int state$waitingForLight__waitFire = 65;
  
  private static final int state$unlockedPanel__waitFire = 66;
  
  private static final int state$idle__waitFire = 67;
  
  private static final int state$active__waitFire = 68;
  
  private static final int state$waitingForDrawer__waitFire = 69;
  
  private static final int state$waitingForLight__waitBook = 70;
  
  private static final int state$unlockedPanel__waitBook = 71;
  
  private static final int state$idle__waitBook = 72;
  
  private static final int state$active__waitBook = 73;
  
  private static final int state$waitingForDrawer__waitBook = 74;
  
  private static final int state$waitingForDrawer__active = 75;
  
  private static final int state$unlockedPanel__active = 76;
  
  private static final int state$idle__active = 77;
  
  private static final int state$active__active = 78;
  
  private static final int state$waitingForLight__active = 79;
  
  void run(java.util.Scanner input, java.io.Writer output) throws java.io.IOException {
    int state = state$idle__idle;
    while (true) {
      String token = input.nextLine();
      switch (state) {
        
        case state$idle__idle: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
             unlockGate(output);
          
             turnBackBookCase(output);
          
             turnBackCandleStick(output);
          
          
          if (doorClosed(token)) {
             state = state$active__idle;
          }
          
          if (gateClosed(token)) {
             state = state$idle__waitingForRest;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__idle: {
          
             unlockGate(output);
          
             turnBackBookCase(output);
          
             turnBackCandleStick(output);
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__idle;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__idle;
          }
          
          if (gateClosed(token)) {
             state = state$active__waitingForRest;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__idle: {
          
             unlockGate(output);
          
             turnBackBookCase(output);
          
             turnBackCandleStick(output);
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__idle;
          }
          
          if (gateClosed(token)) {
             state = state$waitingForDrawer__waitingForRest;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__idle: {
          
             unlockGate(output);
          
             turnBackBookCase(output);
          
             turnBackCandleStick(output);
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__idle;
          }
          
          if (gateClosed(token)) {
             state = state$waitingForLight__waitingForRest;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__idle: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
             unlockGate(output);
          
             turnBackBookCase(output);
          
             turnBackCandleStick(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__idle;
          }
          
          if (gateClosed(token)) {
             state = state$unlockedPanel__waitingForRest;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitingForRest: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitingForRest;
          }
          
          if (fridgeOpened(token)) {
             state = state$unlockedPanel__waitCandleFireBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$unlockedPanel__waitFridgeFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$unlockedPanel__waitFridgeCandleBook;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitingForRest: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitingForRest;
          }
          
          if (fridgeOpened(token)) {
             state = state$idle__waitCandleFireBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$idle__waitFridgeFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$idle__waitFridgeCandleBook;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitingForRest: {
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitingForRest;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitingForRest;
          }
          
          if (fridgeOpened(token)) {
             state = state$active__waitCandleFireBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$active__waitFridgeFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$active__waitFridgeCandleBook;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitingForRest: {
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitingForRest;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForDrawer__waitCandleFireBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForDrawer__waitFridgeFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForDrawer__waitFridgeCandleBook;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitingForRest: {
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitingForRest;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForLight__waitCandleFireBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForLight__waitFridgeFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForLight__waitFridgeCandleBook;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitFridgeCandleBook: {
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitFridgeCandleBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForLight__waitFridgeBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForLight__waitFridgeCandle;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForLight__waitCandleBook;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitFridgeCandleBook: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitFridgeCandleBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$unlockedPanel__waitFridgeBook;
          }
          
          if (bookTaken(token)) {
             state = state$unlockedPanel__waitFridgeCandle;
          }
          
          if (fridgeOpened(token)) {
             state = state$unlockedPanel__waitCandleBook;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitFridgeCandleBook: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitFridgeCandleBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$idle__waitFridgeBook;
          }
          
          if (bookTaken(token)) {
             state = state$idle__waitFridgeCandle;
          }
          
          if (fridgeOpened(token)) {
             state = state$idle__waitCandleBook;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitFridgeCandleBook: {
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitFridgeCandleBook;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitFridgeCandleBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$active__waitFridgeBook;
          }
          
          if (bookTaken(token)) {
             state = state$active__waitFridgeCandle;
          }
          
          if (fridgeOpened(token)) {
             state = state$active__waitCandleBook;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitFridgeCandleBook: {
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitFridgeCandleBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForDrawer__waitFridgeBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForDrawer__waitFridgeCandle;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForDrawer__waitCandleBook;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitFridgeCandle: {
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitFridgeCandle;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForDrawer__waitCandle;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForDrawer__waitFridge;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitFridgeCandle: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitFridgeCandle;
          }
          
          if (fridgeOpened(token)) {
             state = state$unlockedPanel__waitCandle;
          }
          
          if (candleStickTurned(token)) {
             state = state$unlockedPanel__waitFridge;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitFridgeCandle: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitFridgeCandle;
          }
          
          if (fridgeOpened(token)) {
             state = state$idle__waitCandle;
          }
          
          if (candleStickTurned(token)) {
             state = state$idle__waitFridge;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitFridgeCandle: {
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitFridgeCandle;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitFridgeCandle;
          }
          
          if (fridgeOpened(token)) {
             state = state$active__waitCandle;
          }
          
          if (candleStickTurned(token)) {
             state = state$active__waitFridge;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitFridgeCandle: {
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitFridgeCandle;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForLight__waitCandle;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForLight__waitFridge;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitFridgeFireBook: {
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitFridgeFireBook;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForLight__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForLight__waitFridgeBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForLight__waitFridgeFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitFridgeFireBook: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitFridgeFireBook;
          }
          
          if (fridgeOpened(token)) {
             state = state$unlockedPanel__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$unlockedPanel__waitFridgeBook;
          }
          
          if (bookTaken(token)) {
             state = state$unlockedPanel__waitFridgeFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitFridgeFireBook: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitFridgeFireBook;
          }
          
          if (fridgeOpened(token)) {
             state = state$idle__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$idle__waitFridgeBook;
          }
          
          if (bookTaken(token)) {
             state = state$idle__waitFridgeFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitFridgeFireBook: {
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitFridgeFireBook;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitFridgeFireBook;
          }
          
          if (fridgeOpened(token)) {
             state = state$active__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$active__waitFridgeBook;
          }
          
          if (bookTaken(token)) {
             state = state$active__waitFridgeFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitFridgeFireBook: {
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitFridgeFireBook;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForDrawer__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForDrawer__waitFridgeBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForDrawer__waitFridgeFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitFridgeFire: {
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitFridgeFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForDrawer__waitFridge;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForDrawer__waitFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitFridgeFire: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitFridgeFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$unlockedPanel__waitFridge;
          }
          
          if (fridgeOpened(token)) {
             state = state$unlockedPanel__waitFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitFridgeFire: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitFridgeFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$idle__waitFridge;
          }
          
          if (fridgeOpened(token)) {
             state = state$idle__waitFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitFridgeFire: {
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitFridgeFire;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitFridgeFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$active__waitFridge;
          }
          
          if (fridgeOpened(token)) {
             state = state$active__waitFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitFridgeFire: {
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitFridgeFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForLight__waitFridge;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForLight__waitFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitFridgeBook: {
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitFridgeBook;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForDrawer__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForDrawer__waitFridge;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitFridgeBook: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitFridgeBook;
          }
          
          if (fridgeOpened(token)) {
             state = state$unlockedPanel__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$unlockedPanel__waitFridge;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitFridgeBook: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitFridgeBook;
          }
          
          if (fridgeOpened(token)) {
             state = state$idle__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$idle__waitFridge;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitFridgeBook: {
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitFridgeBook;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitFridgeBook;
          }
          
          if (fridgeOpened(token)) {
             state = state$active__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$active__waitFridge;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitFridgeBook: {
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitFridgeBook;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForLight__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForLight__waitFridge;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitFridge: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitFridge;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForLight__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitFridge: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitFridge;
          }
          
          if (fridgeOpened(token)) {
             state = state$unlockedPanel__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitFridge: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitFridge;
          }
          
          if (fridgeOpened(token)) {
             state = state$idle__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitFridge: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitFridge;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitFridge;
          }
          
          if (fridgeOpened(token)) {
             state = state$active__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitFridge: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitFridge;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitingForDrawer__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitCandleFireBook: {
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitCandleFireBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForLight__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForLight__waitCandleBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForLight__waitCandleFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitCandleFireBook: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitCandleFireBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$unlockedPanel__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$unlockedPanel__waitCandleBook;
          }
          
          if (bookTaken(token)) {
             state = state$unlockedPanel__waitCandleFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitCandleFireBook: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitCandleFireBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$idle__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$idle__waitCandleBook;
          }
          
          if (bookTaken(token)) {
             state = state$idle__waitCandleFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitCandleFireBook: {
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitCandleFireBook;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitCandleFireBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$active__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$active__waitCandleBook;
          }
          
          if (bookTaken(token)) {
             state = state$active__waitCandleFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitCandleFireBook: {
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitCandleFireBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForDrawer__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForDrawer__waitCandleBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForDrawer__waitCandleFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitCandleFire: {
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitCandleFire;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForDrawer__waitFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForDrawer__waitCandle;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitCandleFire: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitCandleFire;
          }
          
          if (candleStickTurned(token)) {
             state = state$unlockedPanel__waitFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$unlockedPanel__waitCandle;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitCandleFire: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitCandleFire;
          }
          
          if (candleStickTurned(token)) {
             state = state$idle__waitFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$idle__waitCandle;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitCandleFire: {
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitCandleFire;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitCandleFire;
          }
          
          if (candleStickTurned(token)) {
             state = state$active__waitFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$active__waitCandle;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitCandleFire: {
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitCandleFire;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForLight__waitFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForLight__waitCandle;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitCandleBook: {
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitCandleBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForDrawer__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForDrawer__waitCandle;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitCandleBook: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitCandleBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$unlockedPanel__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$unlockedPanel__waitCandle;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitCandleBook: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitCandleBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$idle__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$idle__waitCandle;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitCandleBook: {
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitCandleBook;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitCandleBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$active__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$active__waitCandle;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitCandleBook: {
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitCandleBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForLight__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForLight__waitCandle;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitCandle: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitCandle;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForLight__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitCandle: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitCandle;
          }
          
          if (candleStickTurned(token)) {
             state = state$unlockedPanel__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitCandle: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitCandle;
          }
          
          if (candleStickTurned(token)) {
             state = state$idle__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitCandle: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitCandle;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitCandle;
          }
          
          if (candleStickTurned(token)) {
             state = state$active__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitCandle: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitCandle;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitingForDrawer__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitFireBook: {
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForDrawer__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForDrawer__waitFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitFireBook: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$unlockedPanel__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$unlockedPanel__waitFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitFireBook: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$idle__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$idle__waitFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitFireBook: {
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitFireBook;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$active__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$active__waitFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitFireBook: {
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForLight__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForLight__waitFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitFire: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForLight__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitFire: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$unlockedPanel__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitFire: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$idle__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitFire: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitFire;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$active__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitFire: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitingForDrawer__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__waitBook: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForLight__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__waitBook: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$unlockedPanel__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__waitBook: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (doorClosed(token)) {
             state = state$active__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$idle__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__waitBook: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__waitBook;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$active__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__waitBook: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForDrawer__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer__active: {
          
             turnBookCase(output);
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel__active;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForDrawer__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$unlockedPanel__active: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
             turnBookCase(output);
          
          
          if (panelClosed(token)) {
             state = state$idle__active;
          }
          
          if (bookTaken(token)) {
             state = state$unlockedPanel__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$idle__active: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
             turnBookCase(output);
          
          
          if (doorClosed(token)) {
             state = state$active__active;
          }
          
          if (bookTaken(token)) {
             state = state$idle__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$active__active: {
          
             turnBookCase(output);
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight__active;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer__active;
          }
          
          if (bookTaken(token)) {
             state = state$active__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
        case state$waitingForLight__active: {
          
             turnBookCase(output);
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel__active;
          }
          
          if (bookTaken(token)) {
             state = state$waitingForLight__active;
          }
          
          if (gateOpened(token)) {
             state = state$idle__idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle__idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle__idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle__idle;
          }
          
          if (fireOut(token)) {
             state = state$idle__idle;
          }
          
          break;
        }
        
      }
    }
  }
  
  private boolean doorClosed(String token) {
    return token.equals("D1CL");
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
  
  private boolean panelClosed(String token) {
    return token.equals("PNCL");
  }
  
  private boolean fridgeOpened(String token) {
    return token.equals("F1OP");
  }
  
  private boolean candleStickTurned(String token) {
    return token.equals("CSTR");
  }
  
  private boolean candleStickTurnedBack(String token) {
    return token.equals("CSTB");
  }
  
  private boolean fireplaceLit(String token) {
    return token.equals("FPLT");
  }
  
  private boolean fireOut(String token) {
    return token.equals("FPOT");
  }
  
  private boolean bookTaken(String token) {
    return token.equals("BKTK");
  }
  
  private boolean bookPlaced(String token) {
    return token.equals("BKPL");
  }
  
  private boolean gateOpened(String token) {
    return token.equals("GTOP");
  }
  
  private boolean gateClosed(String token) {
    return token.equals("GTCL");
  }
  
  
  private void lockGate(java.io.Writer output) throws java.io.IOException {
    output.write("GTLK\n");
    output.flush();
  }
  
  private void turnBookCase(java.io.Writer output) throws java.io.IOException {
    output.write("TRBC\n");
    output.flush();
  }
  
  private void unlockGate(java.io.Writer output) throws java.io.IOException {
    output.write("GTUK\n");
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
  
  private void unlockPanel(java.io.Writer output) throws java.io.IOException {
    output.write("PNUL\n");
    output.flush();
  }
  
  private void lockPanel(java.io.Writer output) throws java.io.IOException {
    output.write("PNLK\n");
    output.flush();
  }
  
  private void turnBackCandleStick(java.io.Writer output) throws java.io.IOException {
    output.write("TBCS\n");
    output.flush();
  }
  
  private void turnBackBookCase(java.io.Writer output) throws java.io.IOException {
    output.write("TBBC\n");
    output.flush();
  }
  
  private void activateTurrets(java.io.Writer output) throws java.io.IOException {
    output.write("ACTU\n");
    output.flush();
  }
  
}