from pathlib import Path
from math import cos, sin, radians
from PIL import Image, ImageDraw, ImageFilter

OUT = Path(__file__).resolve().parent / "installer-assets"
OUT.mkdir(parents=True, exist_ok=True)
S = 4

def gradient(size, top=(9, 19, 36), bottom=(5, 11, 22)):
    w, h = size
    im = Image.new("RGB", (w, h))
    px = im.load()
    for y in range(h):
        t = y / max(1, h - 1)
        c = tuple(round(a * (1 - t) + b * t) for a, b in zip(top, bottom))
        for x in range(w):
            px[x, y] = c
    return im

def ribbon(draw, cx, cy, scale, width):
    # CNN_B breast-cancer ribbon mark, matching the app's existing line logo.
    pts = [(21,55),(36.5,31),(42,22),(40.5,9),(32,9),(23.5,9),(27.5,31),(43,55)]
    pts = [(cx + (x - 32) * scale, cy + (y - 32) * scale) for x,y in pts]
    draw.line(pts[:4], fill=(243,111,177,255), width=width, joint="curve")
    draw.line(pts[3:], fill=(214,58,138,255), width=width, joint="curve")
    links = [((12,22),(32,9)),((52,22),(32,9)),((12,22),(27,30)),((52,22),(37,30)),((12,22),(21,55)),((52,22),(43,55))]
    for a,b in links:
        draw.line([(cx+(a[0]-32)*scale,cy+(a[1]-32)*scale),(cx+(b[0]-32)*scale,cy+(b[1]-32)*scale)],fill=(34,211,238,185),width=max(1,width//3))
    for x,y in [(12,22),(52,22),(32,9),(21,55),(43,55)]:
        r=2.1*scale
        px,py=cx+(x-32)*scale,cy+(y-32)*scale
        draw.ellipse((px-r,py-r,px+r,py+r),fill=(93,232,247,255))

def save_icon():
    n=256*S
    im=gradient((n,n),(13,34,59),(5,11,22)).convert("RGBA")
    glow=Image.new("RGBA",im.size,(0,0,0,0)); gd=ImageDraw.Draw(glow)
    gd.ellipse((20*S,15*S,236*S,231*S),fill=(34,211,238,55))
    glow=glow.filter(ImageFilter.GaussianBlur(35*S)); im.alpha_composite(glow)
    d=ImageDraw.Draw(im)
    d.rounded_rectangle((9*S,9*S,247*S,247*S),radius=54*S,outline=(45,82,117,210),width=2*S)
    ribbon(d,128*S,128*S,3.45*S,7*S)
    im.resize((256,256),Image.Resampling.LANCZOS).save(OUT/"CNN_B.ico",format="ICO",sizes=[(256,256),(128,128),(64,64),(48,48),(32,32),(16,16)])

def save_wizard():
    w,h=164*S,314*S
    im=gradient((w,h),(13,36,63),(5,11,22)).convert("RGBA")
    glow=Image.new("RGBA",im.size,(0,0,0,0)); gd=ImageDraw.Draw(glow)
    gd.ellipse(((-40)*S,20*S,190*S,250*S),fill=(34,211,238,48))
    glow=glow.filter(ImageFilter.GaussianBlur(30*S)); im.alpha_composite(glow)
    d=ImageDraw.Draw(im)
    d.line((23*S,0,23*S,h),fill=(30,51,82,210),width=1*S)
    ribbon(d,82*S,70*S,1.45*S,4*S)
    # Abstract ultrasound fan with luminous scan sweeps; decorative, not a sample scan.
    ox,oy=82*S,172*S
    for rad,alpha in [(42*S,35),(34*S,28),(26*S,24)]:
        box=(ox-rad,oy-rad,ox+rad,oy+rad)
        d.arc(box,start=205,end=335,fill=(89,164,193,alpha+90),width=1*S)
    wedge=[(ox,oy)]
    for a in range(205,336,2):
        r=86*S
        wedge.append((ox+cos(radians(a))*r,oy+sin(radians(a))*r))
    d.polygon(wedge,fill=(12,37,59,120),outline=(34,211,238,90))
    for a in range(210,336,12):
        d.line((ox,oy,ox+cos(radians(a))*88*S,oy+sin(radians(a))*88*S),fill=(70,151,179,55),width=1*S)
    for i in range(12):
        y=oy+22*S+i*4*S
        span=(y-oy)*0.55
        d.line((ox-span,y,ox+span,y),fill=(132,207,216,24+i*2),width=1*S)
    # Signal points lead from the scan toward three classification outputs.
    nodes=[(42,253),(82,268),(122,253)]
    d.line([(42*S,234*S),(82*S,248*S),(122*S,234*S)],fill=(34,211,238,105),width=1*S)
    for i,(x,y) in enumerate(nodes):
        col=[(244,185,66,240),(214,58,138,245),(46,196,182,240)][i]
        r=4*S
        d.ellipse((x*S-r,y*S-r,x*S+r,y*S+r),fill=col)
    d.rounded_rectangle((20*S,294*S,144*S,296*S),radius=S,fill=(34,211,238,145))
    im.resize((164,314),Image.Resampling.LANCZOS).convert("RGB").save(OUT/"wizard.bmp",format="BMP")
    im.resize((55,55),Image.Resampling.LANCZOS).convert("RGB").save(OUT/"wizard-small.bmp",format="BMP")

save_icon()
save_wizard()
print(f"Generados: {OUT/'CNN_B.ico'}, {OUT/'wizard.bmp'}, {OUT/'wizard-small.bmp'}")
