use jni::objects::{JClass, JString, JByteArray};
use jni::sys::jstring;
use jni::JNIEnv;
use qingjian_core::Engine;
use qingjian_dictionary::Dictionary;
use std::sync::{Mutex, OnceLock};

static ENGINE: OnceLock<Mutex<Option<Engine>>> = OnceLock::new();

fn engine() -> &'static Mutex<Option<Engine>> {
    ENGINE.get_or_init(|| Mutex::new(None))
}

fn to_jstring(env: &mut JNIEnv<'_>, value: impl AsRef<str>) -> jstring {
    env.new_string(value.as_ref()).map(|s| s.into_raw()).unwrap_or(std::ptr::null_mut())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_qingjian_android_QingjianNative_init(
    mut env: JNIEnv,
    _class: JClass,
    bytes: JByteArray,
) {
    let data = match env.convert_byte_array(bytes) {
        Ok(v) => v,
        Err(_) => return,
    };
    let text = match String::from_utf8(data) {
        Ok(v) => v,
        Err(_) => return,
    };
    let dict = match Dictionary::parse(&text) {
        Ok(v) => v,
        Err(_) => return,
    };
    if let Ok(mut slot) = engine().lock() {
        *slot = Some(Engine::new(dict));
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_qingjian_android_QingjianNative_push(
    _env: JNIEnv,
    _class: JClass,
    ch: u16,
) {
    if let Ok(mut slot) = engine().lock() {
        if let Some(e) = slot.as_mut() {
            if let Some(c) = char::from_u32(ch as u32) {
                e.push(c);
            }
        }
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_qingjian_android_QingjianNative_backspace(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    if let Ok(mut slot) = engine().lock() {
        if let Some(e) = slot.as_mut() {
            return e.backspace() as jni::sys::jboolean;
        }
    }
    0
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_qingjian_android_QingjianNative_clear(
    _env: JNIEnv,
    _class: JClass,
) {
    if let Ok(mut slot) = engine().lock() {
        if let Some(e) = slot.as_mut() { e.clear(); }
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_qingjian_android_QingjianNative_query(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    let value = if let Ok(slot) = engine().lock() {
        if let Some(e) = slot.as_ref() {
            match e.query() {
                Ok(q) => serde_json::to_string(&q.candidates.items).unwrap_or_else(|_| "[]".into()),
                Err(_) => "[]".into(),
            }
        } else { "[]".into() }
    } else { "[]".into() };
    to_jstring(&mut env, value)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_qingjian_android_QingjianNative_preedit(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    let value = if let Ok(slot) = engine().lock() {
        slot.as_ref().map(|e| e.composition().typed_text().to_owned()).unwrap_or_default()
    } else { String::new() };
    to_jstring(&mut env, value)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_qingjian_android_QingjianNative_commit(
    mut env: JNIEnv,
    _class: JClass,
    index: i32,
) -> jstring {
    let value = if let Ok(mut slot) = engine().lock() {
        if let Some(e) = slot.as_mut() {
            if let Ok(q) = e.query() {
                q.candidates.items.get(index.max(0) as usize).cloned().map(|c| e.commit(&c)).unwrap_or_default()
            } else { String::new() }
        } else { String::new() }
    } else { String::new() };
    to_jstring(&mut env, value)
}
