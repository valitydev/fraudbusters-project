export const TYPE = { STOP: 0, BOOL: 2, BYTE: 3, DOUBLE: 4, I16: 6, I32: 8, I64: 10, STRING: 11, STRUCT: 12,
  MAP: 13, SET: 14, LIST: 15 };
const MESSAGE = { CALL: 1, REPLY: 2, EXCEPTION: 3 };
const VERSION_1 = 0x80010000;

function utf8(text) {
  const bytes = [];
  for (const symbol of String(text)) {
    let code = symbol.codePointAt(0);
    if (code < 0x80) bytes.push(code);
    else if (code < 0x800) bytes.push(0xc0 | (code >> 6), 0x80 | (code & 0x3f));
    else if (code < 0x10000) bytes.push(0xe0 | (code >> 12), 0x80 | ((code >> 6) & 0x3f), 0x80 | (code & 0x3f));
    else {
      bytes.push(0xf0 | (code >> 18), 0x80 | ((code >> 12) & 0x3f), 0x80 | ((code >> 6) & 0x3f), 0x80 | (code & 0x3f));
    }
  }
  return bytes;
}

function fromUtf8(bytes) {
  let text = '';
  for (let index = 0; index < bytes.length;) {
    const first = bytes[index];
    let code;
    if (first < 0x80) {
      code = first; index += 1;
    } else if (first < 0xe0) {
      code = ((first & 0x1f) << 6) | (bytes[index + 1] & 0x3f); index += 2;
    } else if (first < 0xf0) {
      code = ((first & 0x0f) << 12) | ((bytes[index + 1] & 0x3f) << 6) | (bytes[index + 2] & 0x3f); index += 3;
    } else {
      code = ((first & 0x07) << 18) | ((bytes[index + 1] & 0x3f) << 12) | ((bytes[index + 2] & 0x3f) << 6)
        | (bytes[index + 3] & 0x3f);
      index += 4;
    }
    text += String.fromCodePoint(code);
  }
  return text;
}

class Writer {
  constructor() {
    this.bytes = [];
  }

  byte(value) {
    this.bytes.push(value & 0xff);
  }

  i16(value) {
    this.byte(value >> 8);
    this.byte(value);
  }

  i32(value) {
    this.byte(value >>> 24);
    this.byte(value >>> 16);
    this.byte(value >>> 8);
    this.byte(value);
  }

  i64(value) {
    if (!Number.isSafeInteger(value)) throw new Error(`i64 value ${value} is not a safe integer`);
    const high = Math.floor(value / 4294967296);
    this.i32(high | 0);
    this.i32(value - high * 4294967296);
  }

  string(value) {
    const encoded = utf8(value);
    this.i32(encoded.length);
    encoded.forEach((item) => this.byte(item));
  }

  value(type, value, elementType) {
    switch (type) {
      case TYPE.BOOL: this.byte(value ? 1 : 0); break;
      case TYPE.BYTE: this.byte(value); break;
      case TYPE.I16: this.i16(value); break;
      case TYPE.I32: this.i32(value); break;
      case TYPE.I64: this.i64(value); break;
      case TYPE.STRING: this.string(value); break;
      case TYPE.STRUCT: this.struct(value); break;
      case TYPE.LIST:
      case TYPE.SET:
        this.byte(elementType);
        this.i32(value.length);
        value.forEach((item) => this.value(elementType, item));
        break;
      default: throw new Error(`Thrift type ${type} is not supported for writing`);
    }
  }

  struct(fields) {
    fields.filter((field) => field.value !== undefined && field.value !== null).forEach((field) => {
      this.byte(field.type);
      this.i16(field.id);
      this.value(field.type, field.value, field.elementType);
    });
    this.byte(TYPE.STOP);
  }

  buffer() {
    return new Uint8Array(this.bytes).buffer;
  }
}

class Reader {
  constructor(buffer) {
    this.view = new DataView(buffer);
    this.bytes = new Uint8Array(buffer);
    this.offset = 0;
  }

  byte() {
    const value = this.view.getInt8(this.offset);
    this.offset += 1;
    return value;
  }

  i16() {
    const value = this.view.getInt16(this.offset);
    this.offset += 2;
    return value;
  }

  i32() {
    const value = this.view.getInt32(this.offset);
    this.offset += 4;
    return value;
  }

  i64() {
    const high = this.i32();
    const low = this.view.getUint32(this.offset - 0);
    this.offset += 4;
    return high * 4294967296 + low;
  }

  string() {
    const length = this.i32();
    const value = fromUtf8(this.bytes.subarray(this.offset, this.offset + length));
    this.offset += length;
    return value;
  }

  value(type) {
    switch (type) {
      case TYPE.BOOL: return this.byte() !== 0;
      case TYPE.BYTE: return this.byte();
      case TYPE.DOUBLE: {
        const value = this.view.getFloat64(this.offset);
        this.offset += 8;
        return value;
      }
      case TYPE.I16: return this.i16();
      case TYPE.I32: return this.i32();
      case TYPE.I64: return this.i64();
      case TYPE.STRING: return this.string();
      case TYPE.STRUCT: return this.struct();
      case TYPE.LIST:
      case TYPE.SET: {
        const elementType = this.byte();
        const size = this.i32();
        return Array.from({ length: size }, () => this.value(elementType));
      }
      case TYPE.MAP: {
        const keyType = this.byte();
        const valueType = this.byte();
        const size = this.i32();
        const map = {};
        for (let index = 0; index < size; index += 1) map[this.value(keyType)] = this.value(valueType);
        return map;
      }
      default: throw new Error(`Thrift type ${type} is not supported for reading`);
    }
  }

  struct() {
    const result = {};
    for (;;) {
      const type = this.byte();
      if (type === TYPE.STOP) return result;
      const id = this.i16();
      result[id] = this.value(type);
    }
  }
}

export function encodeCall(method, sequenceId, fields) {
  const writer = new Writer();
  writer.i32((VERSION_1 | MESSAGE.CALL) >>> 0);
  writer.string(method);
  writer.i32(sequenceId);
  writer.struct(fields);
  return writer.buffer();
}

export function decodeReply(buffer) {
  const reader = new Reader(buffer);
  const header = reader.i32();
  if ((header & 0xffff0000) !== (VERSION_1 | 0)) throw new Error(`Not a strict binary Thrift message: ${header}`);
  const type = header & 0xff;
  const method = reader.string();
  const sequenceId = reader.i32();
  const body = reader.struct();
  if (type === MESSAGE.EXCEPTION) {
    return { method, sequenceId, applicationError: { message: body[1], type: body[2] } };
  }
  return { method, sequenceId, result: body };
}
