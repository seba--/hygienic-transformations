events
  
  doorClosed D1CL
  
  drawerOpened D2OP
  
  lightOn L1ON
  
  doorOpened D1OP
  
  panelClosed PNCL
  
  fridgeOpened F1OP
  
  candleStickTurned CSTR
  
  candleStickTurnedBack CSTB
  
  fireplaceLit FPLT
  
  fireOut FPOT
  
  bookTaken BKTK
  
  bookPlaced BKPL
  
  gateOpened GTOP
  
  gateClosed GTCL
  
end
resetEvents
  gateOpened
  candleStickTurnedBack
  bookPlaced
  doorOpened
  fireOut
end
commands
  
  lockGate GTLK
  
  turnBookCase TRBC
  
  unlockGate GTUK
  
  lockDoor D1LK
  
  unlockDoor D1UL
  
  unlockPanel PNUL
  
  lockPanel PNLK
  
  turnBackCandleStick TBCS
  
  turnBackBookCase TBBC
  
  activateTurrets ACTU
  
end
state idle__idle
  
  actions {unlockDoor lockPanel unlockGate turnBackBookCase turnBackCandleStick}
  
  
  doorClosed => active__idle
  
  gateClosed => idle__waitingForRest
  
end
state active__idle
  
  actions {unlockGate turnBackBookCase turnBackCandleStick}
  
  
  drawerOpened => waitingForLight__idle
  
  lightOn => waitingForDrawer__idle
  
  gateClosed => active__waitingForRest
  
end
state waitingForDrawer__idle
  
  actions {unlockGate turnBackBookCase turnBackCandleStick}
  
  
  drawerOpened => unlockedPanel__idle
  
  gateClosed => waitingForDrawer__waitingForRest
  
end
state waitingForLight__idle
  
  actions {unlockGate turnBackBookCase turnBackCandleStick}
  
  
  lightOn => unlockedPanel__idle
  
  gateClosed => waitingForLight__waitingForRest
  
end
state unlockedPanel__idle
  
  actions {unlockPanel lockDoor unlockGate turnBackBookCase turnBackCandleStick}
  
  
  panelClosed => idle__idle
  
  gateClosed => unlockedPanel__waitingForRest
  
end
state unlockedPanel__waitingForRest
  
  actions {unlockPanel lockDoor}
  
  
  panelClosed => idle__waitingForRest
  
  fridgeOpened => unlockedPanel__waitCandleFireBook
  
  candleStickTurned => unlockedPanel__waitFridgeFireBook
  
  fireplaceLit => unlockedPanel__waitFridgeCandleBook
  
end
state idle__waitingForRest
  
  actions {unlockDoor lockPanel}
  
  
  doorClosed => active__waitingForRest
  
  fridgeOpened => idle__waitCandleFireBook
  
  candleStickTurned => idle__waitFridgeFireBook
  
  fireplaceLit => idle__waitFridgeCandleBook
  
end
state active__waitingForRest
  
  
  drawerOpened => waitingForLight__waitingForRest
  
  lightOn => waitingForDrawer__waitingForRest
  
  fridgeOpened => active__waitCandleFireBook
  
  candleStickTurned => active__waitFridgeFireBook
  
  fireplaceLit => active__waitFridgeCandleBook
  
end
state waitingForDrawer__waitingForRest
  
  
  drawerOpened => unlockedPanel__waitingForRest
  
  fridgeOpened => waitingForDrawer__waitCandleFireBook
  
  candleStickTurned => waitingForDrawer__waitFridgeFireBook
  
  fireplaceLit => waitingForDrawer__waitFridgeCandleBook
  
end
state waitingForLight__waitingForRest
  
  
  lightOn => unlockedPanel__waitingForRest
  
  fridgeOpened => waitingForLight__waitCandleFireBook
  
  candleStickTurned => waitingForLight__waitFridgeFireBook
  
  fireplaceLit => waitingForLight__waitFridgeCandleBook
  
end
state waitingForLight__waitFridgeCandleBook
  
  
  lightOn => unlockedPanel__waitFridgeCandleBook
  
  candleStickTurned => waitingForLight__waitFridgeBook
  
  bookTaken => waitingForLight__waitFridgeCandle
  
  fridgeOpened => waitingForLight__waitCandleBook
  
end
state unlockedPanel__waitFridgeCandleBook
  
  actions {unlockPanel lockDoor}
  
  
  panelClosed => idle__waitFridgeCandleBook
  
  candleStickTurned => unlockedPanel__waitFridgeBook
  
  bookTaken => unlockedPanel__waitFridgeCandle
  
  fridgeOpened => unlockedPanel__waitCandleBook
  
end
state idle__waitFridgeCandleBook
  
  actions {unlockDoor lockPanel}
  
  
  doorClosed => active__waitFridgeCandleBook
  
  candleStickTurned => idle__waitFridgeBook
  
  bookTaken => idle__waitFridgeCandle
  
  fridgeOpened => idle__waitCandleBook
  
end
state active__waitFridgeCandleBook
  
  
  drawerOpened => waitingForLight__waitFridgeCandleBook
  
  lightOn => waitingForDrawer__waitFridgeCandleBook
  
  candleStickTurned => active__waitFridgeBook
  
  bookTaken => active__waitFridgeCandle
  
  fridgeOpened => active__waitCandleBook
  
end
state waitingForDrawer__waitFridgeCandleBook
  
  
  drawerOpened => unlockedPanel__waitFridgeCandleBook
  
  candleStickTurned => waitingForDrawer__waitFridgeBook
  
  bookTaken => waitingForDrawer__waitFridgeCandle
  
  fridgeOpened => waitingForDrawer__waitCandleBook
  
end
state waitingForDrawer__waitFridgeCandle
  
  
  drawerOpened => unlockedPanel__waitFridgeCandle
  
  fridgeOpened => waitingForDrawer__waitCandle
  
  candleStickTurned => waitingForDrawer__waitFridge
  
end
state unlockedPanel__waitFridgeCandle
  
  actions {unlockPanel lockDoor}
  
  
  panelClosed => idle__waitFridgeCandle
  
  fridgeOpened => unlockedPanel__waitCandle
  
  candleStickTurned => unlockedPanel__waitFridge
  
end
state idle__waitFridgeCandle
  
  actions {unlockDoor lockPanel}
  
  
  doorClosed => active__waitFridgeCandle
  
  fridgeOpened => idle__waitCandle
  
  candleStickTurned => idle__waitFridge
  
end
state active__waitFridgeCandle
  
  
  drawerOpened => waitingForLight__waitFridgeCandle
  
  lightOn => waitingForDrawer__waitFridgeCandle
  
  fridgeOpened => active__waitCandle
  
  candleStickTurned => active__waitFridge
  
end
state waitingForLight__waitFridgeCandle
  
  
  lightOn => unlockedPanel__waitFridgeCandle
  
  fridgeOpened => waitingForLight__waitCandle
  
  candleStickTurned => waitingForLight__waitFridge
  
end
state waitingForLight__waitFridgeFireBook
  
  
  lightOn => unlockedPanel__waitFridgeFireBook
  
  fridgeOpened => waitingForLight__waitFireBook
  
  fireplaceLit => waitingForLight__waitFridgeBook
  
  bookTaken => waitingForLight__waitFridgeFire
  
end
state unlockedPanel__waitFridgeFireBook
  
  actions {unlockPanel lockDoor}
  
  
  panelClosed => idle__waitFridgeFireBook
  
  fridgeOpened => unlockedPanel__waitFireBook
  
  fireplaceLit => unlockedPanel__waitFridgeBook
  
  bookTaken => unlockedPanel__waitFridgeFire
  
end
state idle__waitFridgeFireBook
  
  actions {unlockDoor lockPanel}
  
  
  doorClosed => active__waitFridgeFireBook
  
  fridgeOpened => idle__waitFireBook
  
  fireplaceLit => idle__waitFridgeBook
  
  bookTaken => idle__waitFridgeFire
  
end
state active__waitFridgeFireBook
  
  
  drawerOpened => waitingForLight__waitFridgeFireBook
  
  lightOn => waitingForDrawer__waitFridgeFireBook
  
  fridgeOpened => active__waitFireBook
  
  fireplaceLit => active__waitFridgeBook
  
  bookTaken => active__waitFridgeFire
  
end
state waitingForDrawer__waitFridgeFireBook
  
  
  drawerOpened => unlockedPanel__waitFridgeFireBook
  
  fridgeOpened => waitingForDrawer__waitFireBook
  
  fireplaceLit => waitingForDrawer__waitFridgeBook
  
  bookTaken => waitingForDrawer__waitFridgeFire
  
end
state waitingForDrawer__waitFridgeFire
  
  
  drawerOpened => unlockedPanel__waitFridgeFire
  
  fireplaceLit => waitingForDrawer__waitFridge
  
  fridgeOpened => waitingForDrawer__waitFire
  
end
state unlockedPanel__waitFridgeFire
  
  actions {unlockPanel lockDoor}
  
  
  panelClosed => idle__waitFridgeFire
  
  fireplaceLit => unlockedPanel__waitFridge
  
  fridgeOpened => unlockedPanel__waitFire
  
end
state idle__waitFridgeFire
  
  actions {unlockDoor lockPanel}
  
  
  doorClosed => active__waitFridgeFire
  
  fireplaceLit => idle__waitFridge
  
  fridgeOpened => idle__waitFire
  
end
state active__waitFridgeFire
  
  
  drawerOpened => waitingForLight__waitFridgeFire
  
  lightOn => waitingForDrawer__waitFridgeFire
  
  fireplaceLit => active__waitFridge
  
  fridgeOpened => active__waitFire
  
end
state waitingForLight__waitFridgeFire
  
  
  lightOn => unlockedPanel__waitFridgeFire
  
  fireplaceLit => waitingForLight__waitFridge
  
  fridgeOpened => waitingForLight__waitFire
  
end
state waitingForDrawer__waitFridgeBook
  
  
  drawerOpened => unlockedPanel__waitFridgeBook
  
  fridgeOpened => waitingForDrawer__waitBook
  
  bookTaken => waitingForDrawer__waitFridge
  
end
state unlockedPanel__waitFridgeBook
  
  actions {unlockPanel lockDoor}
  
  
  panelClosed => idle__waitFridgeBook
  
  fridgeOpened => unlockedPanel__waitBook
  
  bookTaken => unlockedPanel__waitFridge
  
end
state idle__waitFridgeBook
  
  actions {unlockDoor lockPanel}
  
  
  doorClosed => active__waitFridgeBook
  
  fridgeOpened => idle__waitBook
  
  bookTaken => idle__waitFridge
  
end
state active__waitFridgeBook
  
  
  drawerOpened => waitingForLight__waitFridgeBook
  
  lightOn => waitingForDrawer__waitFridgeBook
  
  fridgeOpened => active__waitBook
  
  bookTaken => active__waitFridge
  
end
state waitingForLight__waitFridgeBook
  
  
  lightOn => unlockedPanel__waitFridgeBook
  
  fridgeOpened => waitingForLight__waitBook
  
  bookTaken => waitingForLight__waitFridge
  
end
state waitingForLight__waitFridge
  
  actions {lockGate activateTurrets}
  
  
  lightOn => unlockedPanel__waitFridge
  
  fridgeOpened => waitingForLight__active
  
end
state unlockedPanel__waitFridge
  
  actions {unlockPanel lockDoor lockGate activateTurrets}
  
  
  panelClosed => idle__waitFridge
  
  fridgeOpened => unlockedPanel__active
  
end
state idle__waitFridge
  
  actions {unlockDoor lockPanel lockGate activateTurrets}
  
  
  doorClosed => active__waitFridge
  
  fridgeOpened => idle__active
  
end
state active__waitFridge
  
  actions {lockGate activateTurrets}
  
  
  drawerOpened => waitingForLight__waitFridge
  
  lightOn => waitingForDrawer__waitFridge
  
  fridgeOpened => active__active
  
end
state waitingForDrawer__waitFridge
  
  actions {lockGate activateTurrets}
  
  
  drawerOpened => unlockedPanel__waitFridge
  
  fridgeOpened => waitingForDrawer__active
  
end
state waitingForLight__waitCandleFireBook
  
  
  lightOn => unlockedPanel__waitCandleFireBook
  
  candleStickTurned => waitingForLight__waitFireBook
  
  fireplaceLit => waitingForLight__waitCandleBook
  
  bookTaken => waitingForLight__waitCandleFire
  
end
state unlockedPanel__waitCandleFireBook
  
  actions {unlockPanel lockDoor}
  
  
  panelClosed => idle__waitCandleFireBook
  
  candleStickTurned => unlockedPanel__waitFireBook
  
  fireplaceLit => unlockedPanel__waitCandleBook
  
  bookTaken => unlockedPanel__waitCandleFire
  
end
state idle__waitCandleFireBook
  
  actions {unlockDoor lockPanel}
  
  
  doorClosed => active__waitCandleFireBook
  
  candleStickTurned => idle__waitFireBook
  
  fireplaceLit => idle__waitCandleBook
  
  bookTaken => idle__waitCandleFire
  
end
state active__waitCandleFireBook
  
  
  drawerOpened => waitingForLight__waitCandleFireBook
  
  lightOn => waitingForDrawer__waitCandleFireBook
  
  candleStickTurned => active__waitFireBook
  
  fireplaceLit => active__waitCandleBook
  
  bookTaken => active__waitCandleFire
  
end
state waitingForDrawer__waitCandleFireBook
  
  
  drawerOpened => unlockedPanel__waitCandleFireBook
  
  candleStickTurned => waitingForDrawer__waitFireBook
  
  fireplaceLit => waitingForDrawer__waitCandleBook
  
  bookTaken => waitingForDrawer__waitCandleFire
  
end
state waitingForDrawer__waitCandleFire
  
  
  drawerOpened => unlockedPanel__waitCandleFire
  
  candleStickTurned => waitingForDrawer__waitFire
  
  fireplaceLit => waitingForDrawer__waitCandle
  
end
state unlockedPanel__waitCandleFire
  
  actions {unlockPanel lockDoor}
  
  
  panelClosed => idle__waitCandleFire
  
  candleStickTurned => unlockedPanel__waitFire
  
  fireplaceLit => unlockedPanel__waitCandle
  
end
state idle__waitCandleFire
  
  actions {unlockDoor lockPanel}
  
  
  doorClosed => active__waitCandleFire
  
  candleStickTurned => idle__waitFire
  
  fireplaceLit => idle__waitCandle
  
end
state active__waitCandleFire
  
  
  drawerOpened => waitingForLight__waitCandleFire
  
  lightOn => waitingForDrawer__waitCandleFire
  
  candleStickTurned => active__waitFire
  
  fireplaceLit => active__waitCandle
  
end
state waitingForLight__waitCandleFire
  
  
  lightOn => unlockedPanel__waitCandleFire
  
  candleStickTurned => waitingForLight__waitFire
  
  fireplaceLit => waitingForLight__waitCandle
  
end
state waitingForDrawer__waitCandleBook
  
  
  drawerOpened => unlockedPanel__waitCandleBook
  
  candleStickTurned => waitingForDrawer__waitBook
  
  bookTaken => waitingForDrawer__waitCandle
  
end
state unlockedPanel__waitCandleBook
  
  actions {unlockPanel lockDoor}
  
  
  panelClosed => idle__waitCandleBook
  
  candleStickTurned => unlockedPanel__waitBook
  
  bookTaken => unlockedPanel__waitCandle
  
end
state idle__waitCandleBook
  
  actions {unlockDoor lockPanel}
  
  
  doorClosed => active__waitCandleBook
  
  candleStickTurned => idle__waitBook
  
  bookTaken => idle__waitCandle
  
end
state active__waitCandleBook
  
  
  drawerOpened => waitingForLight__waitCandleBook
  
  lightOn => waitingForDrawer__waitCandleBook
  
  candleStickTurned => active__waitBook
  
  bookTaken => active__waitCandle
  
end
state waitingForLight__waitCandleBook
  
  
  lightOn => unlockedPanel__waitCandleBook
  
  candleStickTurned => waitingForLight__waitBook
  
  bookTaken => waitingForLight__waitCandle
  
end
state waitingForLight__waitCandle
  
  actions {lockGate activateTurrets}
  
  
  lightOn => unlockedPanel__waitCandle
  
  candleStickTurned => waitingForLight__active
  
end
state unlockedPanel__waitCandle
  
  actions {unlockPanel lockDoor lockGate activateTurrets}
  
  
  panelClosed => idle__waitCandle
  
  candleStickTurned => unlockedPanel__active
  
end
state idle__waitCandle
  
  actions {unlockDoor lockPanel lockGate activateTurrets}
  
  
  doorClosed => active__waitCandle
  
  candleStickTurned => idle__active
  
end
state active__waitCandle
  
  actions {lockGate activateTurrets}
  
  
  drawerOpened => waitingForLight__waitCandle
  
  lightOn => waitingForDrawer__waitCandle
  
  candleStickTurned => active__active
  
end
state waitingForDrawer__waitCandle
  
  actions {lockGate activateTurrets}
  
  
  drawerOpened => unlockedPanel__waitCandle
  
  candleStickTurned => waitingForDrawer__active
  
end
state waitingForDrawer__waitFireBook
  
  
  drawerOpened => unlockedPanel__waitFireBook
  
  fireplaceLit => waitingForDrawer__waitBook
  
  bookTaken => waitingForDrawer__waitFire
  
end
state unlockedPanel__waitFireBook
  
  actions {unlockPanel lockDoor}
  
  
  panelClosed => idle__waitFireBook
  
  fireplaceLit => unlockedPanel__waitBook
  
  bookTaken => unlockedPanel__waitFire
  
end
state idle__waitFireBook
  
  actions {unlockDoor lockPanel}
  
  
  doorClosed => active__waitFireBook
  
  fireplaceLit => idle__waitBook
  
  bookTaken => idle__waitFire
  
end
state active__waitFireBook
  
  
  drawerOpened => waitingForLight__waitFireBook
  
  lightOn => waitingForDrawer__waitFireBook
  
  fireplaceLit => active__waitBook
  
  bookTaken => active__waitFire
  
end
state waitingForLight__waitFireBook
  
  
  lightOn => unlockedPanel__waitFireBook
  
  fireplaceLit => waitingForLight__waitBook
  
  bookTaken => waitingForLight__waitFire
  
end
state waitingForLight__waitFire
  
  actions {lockGate activateTurrets}
  
  
  lightOn => unlockedPanel__waitFire
  
  fireplaceLit => waitingForLight__active
  
end
state unlockedPanel__waitFire
  
  actions {unlockPanel lockDoor lockGate activateTurrets}
  
  
  panelClosed => idle__waitFire
  
  fireplaceLit => unlockedPanel__active
  
end
state idle__waitFire
  
  actions {unlockDoor lockPanel lockGate activateTurrets}
  
  
  doorClosed => active__waitFire
  
  fireplaceLit => idle__active
  
end
state active__waitFire
  
  actions {lockGate activateTurrets}
  
  
  drawerOpened => waitingForLight__waitFire
  
  lightOn => waitingForDrawer__waitFire
  
  fireplaceLit => active__active
  
end
state waitingForDrawer__waitFire
  
  actions {lockGate activateTurrets}
  
  
  drawerOpened => unlockedPanel__waitFire
  
  fireplaceLit => waitingForDrawer__active
  
end
state waitingForLight__waitBook
  
  actions {lockGate activateTurrets}
  
  
  lightOn => unlockedPanel__waitBook
  
  bookTaken => waitingForLight__active
  
end
state unlockedPanel__waitBook
  
  actions {unlockPanel lockDoor lockGate activateTurrets}
  
  
  panelClosed => idle__waitBook
  
  bookTaken => unlockedPanel__active
  
end
state idle__waitBook
  
  actions {unlockDoor lockPanel lockGate activateTurrets}
  
  
  doorClosed => active__waitBook
  
  bookTaken => idle__active
  
end
state active__waitBook
  
  actions {lockGate activateTurrets}
  
  
  drawerOpened => waitingForLight__waitBook
  
  lightOn => waitingForDrawer__waitBook
  
  bookTaken => active__active
  
end
state waitingForDrawer__waitBook
  
  actions {lockGate activateTurrets}
  
  
  drawerOpened => unlockedPanel__waitBook
  
  bookTaken => waitingForDrawer__active
  
end
state waitingForDrawer__active
  
  actions {turnBookCase}
  
  
  drawerOpened => unlockedPanel__active
  
  bookTaken => waitingForDrawer__active
  
end
state unlockedPanel__active
  
  actions {unlockPanel lockDoor turnBookCase}
  
  
  panelClosed => idle__active
  
  bookTaken => unlockedPanel__active
  
end
state idle__active
  
  actions {unlockDoor lockPanel turnBookCase}
  
  
  doorClosed => active__active
  
  bookTaken => idle__active
  
end
state active__active
  
  actions {turnBookCase}
  
  
  drawerOpened => waitingForLight__active
  
  lightOn => waitingForDrawer__active
  
  bookTaken => active__active
  
end
state waitingForLight__active
  
  actions {turnBookCase}
  
  
  lightOn => unlockedPanel__active
  
  bookTaken => waitingForLight__active
  
end