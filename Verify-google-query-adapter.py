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
assert apk.get_androidversion_name() == "17.67.17.ve.arm64"
assert apk.get_androidversion_code() == "301822338"
with zipfile.ZipFile(sys.argv[1]) as z:
    executor = next(c for c in DEX(z.read("classes.dex")).get_classes() if c.get_name() == "Larku;")
    proto = next(c for c in DEX(z.read("classes10.dex")).get_classes() if c.get_name() == "Lgdka;")
    assert any(f.get_name() == "l" and f.get_descriptor() == "Ljava/lang/String;" for f in proto.get_fields())
    builder = next(m for m in executor.get_methods() if m.get_name() == "d")
    assert builder.get_descriptor().replace(" ", "") == "(Ljava/lang/String;Lgdka;Landroid/os/Bundle;Laxno;)Landroid/content/Intent;"
    search = next(m for m in executor.get_methods() if m.get_name() == "j")
    instructions = list(search.get_instructions())
    calls = [i for i, instruction in enumerate(instructions)
             if instruction.get_name().startswith("invoke-") and "->playFromSearch(" in instruction.get_output()]
    assert len(calls) == 1
    call_index = calls[0]
    assert any("Lgdka;->l Ljava/lang/String;" in instruction.get_output()
               for instruction in instructions[max(0, call_index - 4):call_index])
print("Pinned Google version, intent-builder signature and music-search field use verified")

# Fixed request-turn schema: inspect only named fields, never runtime conversation dumps.
with zipfile.ZipFile(sys.argv[1]) as z:
    classes = {c.get_name(): c for c in DEX(z.read("classes12.dex")).get_classes()}
    expected = {
        "Lartg;": {"i": "Ljava/lang/String;", "d": "Laofa;"},
        "Laofa;": {"u": "Lhent;"},
        "Laofk;": {"d": "Laoft;", "c": "Ljava/util/List;"},
        "Laoft;": {"a": "Laofx;"},
        "Laofe;": {"a": "Laofx;"},
        "Laofx;": {"a": "Lappq;", "b": "Lapvs;", "g": "Z"},
        "Lapvs;": {"a": "Ljava/lang/String;", "l": "Ljava/lang/String;"},
    }
    for name, fields in expected.items():
        actual = {f.get_name(): f.get_descriptor() for f in classes[name].get_fields()}
        for key, value in fields.items():
            assert actual.get(key) == value, (name, key, actual.get(key))
    assert any(m.get_name() == "b" and m.get_descriptor() == "()Ljava/lang/String;"
               for m in classes["Lappq;"].get_methods())
    assert search.get_descriptor().replace(" ", "") == "(Ljava/lang/String;Lgdka;Lgbuc;Laxno;Lartg;Landroid/os/Bundle;Lhdtz;)Ljava/lang/Object;"
print("Pinned exact-request turn/message schema and executor signature verified")

# Coverage regression: capture must precede both foreground and native/search branches.
common = next(m for m in executor.get_methods() if m.get_name() == "o")
assert common.get_descriptor().replace(" ", "") == "(Lgdka;Lgbuc;Laxno;Lartg;Lhdtz;)Ljava/lang/Object;"
references = [i.get_output() for i in common.get_instructions() if i.get_name().startswith("invoke-")]
assert any("Larku;->j(" in r for r in references)
assert any("Larku;->B(" in r for r in references)
foreground_gate = next(m for m in executor.get_methods() if m.get_name() == "B")
assert any("Larku;->l(" in i.get_output() for i in foreground_gate.get_instructions() if i.get_name().startswith("invoke-"))
with zipfile.ZipFile(sys.argv[1]) as z:
    provider = next(c for c in DEX(z.read("classes2.dex")).get_classes() if c.get_name() == "Lgbqj;")
    fields = {f.get_name(): f.get_descriptor() for f in provider.get_fields()}
    assert fields["c"] == "Lgbqj;" and fields["r"] == "I"
print("Common pre-branch dispatcher and YouTube Music provider schema verified")

# Queue fallback is tied to committed new-user input, not to the music executor.
with zipfile.ZipFile(sys.argv[1]) as z:
    live = {c.get_name(): c for c in DEX(z.read("classes12.dex")).get_classes()}
    for name, fields in {
        "Laobd;": {"l":"Laobk;", "m":"Lapvs;", "t":"Lappx;", "k":"Lanjy;", "u":"Laofw;"},
        "Laobk;": {"c":"Laofa;"},
    }.items():
        actual={f.get_name():f.get_descriptor() for f in live[name].get_fields()}
        for key,value in fields.items(): assert actual.get(key)==value,(name,key,actual.get(key))
    submit=next(m for m in live["Laobd;"].get_methods() if m.get_name()=="invokeSuspend")
    assert submit.get_descriptor()=="(Ljava/lang/Object;)Ljava/lang/Object;"
    refs=[i.get_output() for i in submit.get_instructions()]
    for expected in ["Laobd;->m Lapvs;","Laofp;->j(Lapso;)","Laofx;-><init>(Lappq; Lapvs;", "Laofk;->K(Laofx; Lhemx; Z)","Lhemx;->h(Ljava/lang/Object; Ljava/lang/Object;)Z"]:
        assert any(expected in ref for ref in refs),expected
    assert any(i.get_name()=="instance-of" and "Lanjl;" in i.get_output() for i in submit.get_instructions())
print("Pinned new-user coroutine, exact input identity, new-turn branch and state commit verified")

# Semantic category check: a class merely referenced by invokeSuspend need not be new input.
with zipfile.ZipFile(sys.argv[1]) as z:
    live={c.get_name():c for c in DEX(z.read("classes12.dex")).get_classes()}
    def label(c):return "\n".join(i.get_output() for m in live[c].get_methods() if m.get_name()=="toString" for i in m.get_instructions())
    assert '"Default"' in label("Lanjn;")
    assert '"FollowOnInput"' in label("Lanjr;")
    assert 'Regenerate(' in label("Lanjt;")
    assert 'Lanjl;' in live["Lanjn;"].get_interfaces()
    assert 'Lanjl;' in live["Lanjr;"].get_interfaces()
print("17.67 Default/FollowOn versus regeneration input semantics verified")
