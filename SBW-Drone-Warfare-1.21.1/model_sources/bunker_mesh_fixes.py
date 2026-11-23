"""Reapply only the reviewed binary mesh fixes, once, after exporting an original bunker mesh.
Run from any directory. Original and fixed input SHA256s are enforced; no PNG is edited.
The supplied .bbmodel files remain the original reference, including decorative dark overlays.
"""
from pathlib import Path
import hashlib,json,struct
root=Path(__file__).resolve().parents[1]
rules=json.loads((root/'model_sources/bunker-edge-repair.json').read_text())
for name,rule in rules.items():
 p=root/f'src/main/resources/assets/sbwdroneconfig/{name}/{name}.bin';data=p.read_bytes();digest=hashlib.sha256(data).hexdigest()
 if digest==rule['after']:continue
 assert digest==rule['before'],f'Unrecognized {name} export: {digest}'
 if name=='pb01':
  out=bytearray(data)
  for at in rule['faceByteOffsets']:
   values=struct.unpack_from('>23f',data,at);v=[values[k:k+5]for k in (3,8,13,18)]
   struct.pack_into('>23f',out,at,*(tuple(-x for x in values[:3])+v[0]+v[2]+v[1]+v[1]))
 else:
  source=json.loads((root/'model_sources/ps01/PROTECTIVE_SHELTER_PS01.bbmodel').read_text(encoding='utf-8'))
  key=lambda vs:tuple(sorted(set(tuple(round(x,5)for x in v)for v in vs)))
  remove={key([tuple(x/16 for x in e['vertices'][v])for v in f['vertices']])for e in source['elements']if e['name'].startswith('pier_bottom_damp_')for f in e['faces'].values()}
  out=bytearray(data[:8]);at=8;removed=0
  for _ in range(struct.unpack_from('>i',data,4)[0]):
   group,texture,count=struct.unpack_from('>iii',data,at);at+=12;faces=[]
   for _ in range(count):
    face=data[at:at+92];values=struct.unpack('>23f',face);at+=92
    if group==0 and key([values[k:k+3]for k in (3,8,13,18)])in remove:removed+=1
    else:faces.append(face)
   out.extend(struct.pack('>iii',group,texture,len(faces)));out.extend(b''.join(faces))
  assert removed==24
 assert hashlib.sha256(out).hexdigest()==rule['after'],'Unexpected repair output'
 p.write_bytes(out)
