INCLUDE "gbhw.inc"
INCLUDE "ibmpc1.inc"
INCLUDE "memory.inc"

SECTION "savevalue",HOME[$d2]
; We get here immidiately after reading the desired IO port,
; because the first byte of the ROM header after the initial 3 user bytes
; is $ce, (part of the nintendo logo) thus the sequence 'db $18 $ce' means 'jr $d2'
	di
	ld   [$ff80], A                ; Will be read by vblank_handler
	jp   begin

SECTION "Org $100",HOME[$100]
; --- Choose ONE ---
;	ld   A, [$ff40]        ; LCDC
	ld   A, [$ff41]        ; STAT
; ------------------
	db   $18               ; jr

; NOTE: ROM_HEADER macro should not be at the begin of a line
;       the assembler will think it is a label!
	ROM_HEADER      ROM_NOMBC, ROM_SIZE_32KBYTE, RAM_SIZE_0KBYTE

TileData:
        chr_IBMPC1      1,8

begin:
	di
	ld sp, $d000
	call    StopLCD

	ld      a,$e4
	ld      [rBGP],a        ; Setup the default background palette

	ld      a,0
	ld      [rSCX],a
	ld      [rSCY],a

; Copy ibmpc1 font to WRAM
	ld      hl,TileData
	ld      de,$8000
	ld      bc,8*256        ; length (8 bytes per tile) x (256 tiles)
	call    mem_CopyMono    ; Copy tile data to memory

; clear BG tile map (fill with ascii $20)
	ld      a,$20
	ld      hl,$9800
	ld      bc,SCRN_VX_B * SCRN_VY_B
	call    mem_Set

; Display value we read (stored at $ff80)  // FIXME: Will fail if value contains values 'a'-'f'
	ld   hl, $9800
; Value is displayed in HEX so prepend '0x'
	ld   a, "0"
	ldi  [hl], a
	ld   a, "x"
	ldi  [hl], a
	ld   a, [$ff80]
	ld   b, 4
shr4:
	rra
	dec  b
	jr   nz, shr4
	and  a, $0f
	add  a, "0"
	ldi  [hl],a
	ld   a, [$ff80]
	and  a, $0f
	add  a, "0"
	ldi  [hl],a

; Now we turn on the LCD display to view the results!
	ld      a,LCDCF_ON|LCDCF_BG8000|LCDCF_BG9800|LCDCF_BGON|LCDCF_OBJ16|LCDCF_OBJOFF
	ld      [rLCDC],a       ; Turn screen on

; Since we have accomplished our goal, we now have nothing
; else to do. As a result, we just Jump to a label that
; causes an infinite loop condition to occur.

.wait:
	jp      .wait


; *** Turn off the LCD display ***
StopLCD:
	ld      a,[rLCDC]
	rlca                    ; Put the high bit of LCDC into the Carry flag
	ret     nc              ; Screen is off already. Exit.

; Loop until we are in VBlank
.wait:
	ld      a,[rLY]
	cp      145             ; Is display on scan line 145 yet?
	jr      nz,.wait        ; no, keep waiting

; Turn off the LCD
	ld      a,[rLCDC]
	res     7,a             ; Reset bit 7 of LCDC
	ld      [rLCDC],a

	ret

vblank_handler:

;* End of File *

