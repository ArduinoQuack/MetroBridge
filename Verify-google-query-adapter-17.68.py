#!/usr/bin/env python3
"""Check the pinned private music boundary against the supplied APK (androguard required).
Usage: python3 Verify-google-query-adapter.py /path/to/google/base.apk
Does not prove that the runtime query is populated or that ART invokes the hook.
"""
import sys
import zipfile
from loguru import logger
logger.remove()
from androguard.core.apk import APK
from androguard.core.dex import DEX

apk = APK(sys.argv[1])
assert apk.get_package() == "com.google.android.googlequicksearchbox"
assert apk.get_androidversion_name() == "17.68.17.ve.arm64"
assert apk.get_androidversion_code() == "301824130"
with zipfile.ZipFile(sys.argv[1]) as z:
    executor = next(c for c in DEX(z.read("classes.dex")).get_classes() if c.get_name() == "Larma;")
    proto = next(c for c in DEX(z.read("classes10.dex")).get_classes() if c.get_name() == "Lgdqi;")
    assert any(f.get_name() == "l" and f.get_descriptor() == "Ljava/lang/String;" for f in proto.get_fields())
    builder = next(m for m in executor.get_methods() if m.get_name() == "d")
    assert builder.get_descriptor().replace(" ", "") == "(Ljava/lang/String;Lgdqi;Landroid/os/Bundle;Laxsa;)Landroid/content/Intent;"
    search = next(m for m in executor.get_methods() if m.get_name() == "j")
    instructions = list(search.get_instructions())
    calls = [i for i, instruction in enumerate(instructions)
             if instruction.get_name().startswith("invoke-") and "->playFromSearch(" in instruction.get_output()]
    assert len(calls) == 1
    call_index = calls[0]
    assert any("Lgdqi;->l Ljava/lang/String;" in instruction.get_output()
               for instruction in instructions[max(0, call_index - 4):call_index])
print("Pinned Google version, intent-builder signature and music-search field use verified")

# Fixed request-turn schema: inspect only named fields, never runtime conversation dumps.
with zipfile.ZipFile(sys.argv[1]) as z:
    classes = {c.get_name(): c for c in DEX(z.read("classes12.dex")).get_classes()}
    expected = {
        "Larut;": {"i": "Ljava/lang/String;", "d": "Laohd;"},
        "Laohd;": {"u": "Lhesz;"},
        "Laohn;": {"d": "Laohw;", "c": "Ljava/util/List;"},
        "Laohw;": {"a": "Laoia;"},
        "Laohh;": {"a": "Laoia;"},
        "Laoia;": {"a": "Lapqo;", "b": "Lapwy;", "g": "Z"},
        "Lapwy;": {"a": "Ljava/lang/String;", "m": "Ljava/lang/String;"},
    }
    for name, fields in expected.items():
        actual = {f.get_name(): f.get_descriptor() for f in classes[name].get_fields()}
        for key, value in fields.items():
            assert actual.get(key) == value, (name, key, actual.get(key))
    assert any(m.get_name() == "b" and m.get_descriptor() == "()Ljava/lang/String;" for m in classes["Lapqn;"].get_methods())
    assert search.get_descriptor().replace(" ", "") == "(Ljava/lang/String;Lgdqi;Lgbzw;Laxsa;Larut;Landroid/os/Bundle;Lhdze;)Ljava/lang/Object;"
print("Pinned exact-request turn/message schema and executor signature verified")

# Coverage regression: capture must precede both foreground and native/search branches.
common = next(m for m in executor.get_methods() if m.get_name() == "o")
assert common.get_descriptor().replace(" ", "") == "(Lgdqi;Lgbzw;Laxsa;Larut;Lhdze;)Ljava/lang/Object;"
references = [i.get_output() for i in common.get_instructions() if i.get_name().startswith("invoke-")]
assert any("Larma;->j(" in r for r in references)
assert any("Larma;->B(" in r for r in references)
foreground_gate = next(m for m in executor.get_methods() if m.get_name() == "B")
assert any("Larma;->l(" in i.get_output() for i in foreground_gate.get_instructions() if i.get_name().startswith("invoke-"))
with zipfile.ZipFile(sys.argv[1]) as z:
    provider = next(c for c in DEX(z.read("classes2.dex")).get_classes() if c.get_name() == "Lgbwc;")
    fields = {f.get_name(): f.get_descriptor() for f in provider.get_fields()}
    assert fields["c"] == "Lgbwc;" and fields["r"] == "I"
print("Common pre-branch dispatcher and YouTube Music provider schema verified")

# Queue fallback is tied to committed new-user input, not to the music executor.
with zipfile.ZipFile(sys.argv[1]) as z:
    live = {c.get_name(): c for c in DEX(z.read("classes12.dex")).get_classes()}
    for name, fields in {
        "Laodg;": {"l":"Laodn;", "m":"Lapwy;", "t":"Lapqv;", "k":"Lanlv;", "u":"Laohz;"},
        "Laodn;": {"c":"Laohd;"},
    }.items():
        actual={f.get_name():f.get_descriptor() for f in live[name].get_fields()}
        for key,value in fields.items(): assert actual.get(key)==value,(name,key,actual.get(key))
    submit=next(m for m in live["Laodg;"].get_methods() if m.get_name()=="invokeSuspend")
    assert submit.get_descriptor()=="(Ljava/lang/Object;)Ljava/lang/Object;"
    refs=[i.get_output() for i in submit.get_instructions()]
    for expected in ["Laodg;->m Lapwy;","Laohs;->j(Laptm;)","Laoia;-><init>(Lapqo; Lapwy;", "Laohn;->L(Laoia; Lhesd; Z)","Lhesd;->h(Ljava/lang/Object; Ljava/lang/Object;)Z"]:
        assert any(expected in ref for ref in refs),expected
    assert any(i.get_name()=="instance-of" and "Lanli;" in i.get_output() for i in submit.get_instructions())
    def label(c):return "\n".join(i.get_output() for m in live[c].get_methods() if m.get_name()=="toString" for i in m.get_instructions())
    assert '"Default"' in label("Lanlk;")
    assert '"FollowOnInput"' in label("Lanlo;")
    assert 'Regenerate(' in label("Lanlq;")
    assert 'Lanli;' in live["Lanlk;"].get_interfaces()
    assert 'Lanli;' in live["Lanlo;"].get_interfaces()
print("Pinned new-user coroutine, exact input identity, new-turn branch and state commit verified")

# Exact reply correlation, including null requestId on the observed streaming path.
with zipfile.ZipFile(sys.argv[1]) as z:
    classes={c.get_name():c for c in DEX(z.read("classes12.dex")).get_classes()}
    helper=next(m for m in classes["Laohs;"].get_methods() if m.get_name()=="i")
    assert helper.get_descriptor().replace(" ","")=="(Laptm;)Lapqm;"
    model={f.get_name():f.get_descriptor() for f in classes["Lapqm;"].get_fields()}
    assert model["a"]=="Ljava/lang/String;"
    refs="\n".join(i.get_output() for m in classes["Laobm;"].get_methods() if m.get_name()=="invokeSuspend" for i in m.get_instructions())
    for anchor in ["Laohs;->i(Laptm;)", "Lapqm;->a Ljava/lang/String;", "Lawld;-><init>(Ljava/lang/String; Ljava/lang/String;)", "Lawlf;->t(Lawky; Lawld; Lawnq;"]:
        assert anchor in refs,anchor
print("Native helper-derived response ID and exact stream-message construction verified")
