import elements from "@webref/elements";
import idl from "@webref/idl";
import {mkdir, writeFile} from "node:fs/promises";
import {dirname, resolve} from "node:path";

const output = resolve("build/generated/webref/elements.json");
const [elementSpecs, idlSpecs] = await Promise.all([elements.listAll(), idl.parseAll()]);

const definitions = new Map();
const includes = new Map();
const enums = new Set();
const obsoleteMembers = new WeakSet();
let foundObsoleteHtmlIdl = false;

for (const [shortname, ast] of Object.entries(idlSpecs)) {
  let obsolete = false;
  for (const definition of ast) {
    // The HTML specification emits its obsolete element and attribute IDL as one final block.
    if (shortname === "html" && definition.name === "HTMLMarqueeElement") {
      obsolete = true;
      foundObsoleteHtmlIdl = true;
    }
    if (definition.type === "enum") {
      enums.add(definition.name);
    }
    if (definition.type === "includes") {
      const targets = includes.get(definition.target) ?? [];
      targets.push(definition.includes);
      includes.set(definition.target, targets);
      continue;
    }
    if (definition.type !== "interface" && definition.type
        !== "interface mixin") {
      continue;
    }
    if (obsolete) {
      definition.members.forEach(member => obsoleteMembers.add(member));
    }
    const existing = definitions.get(definition.name) ?? {
      inheritance: null, members: [],
    };
    if (!definition.partial && definition.inheritance) {
      if (existing.inheritance
          && existing.inheritance !== definition.inheritance) {
        throw new Error(
            `Conflicting inheritance for ${definition.name}: `
            + `${existing.inheritance} and ${definition.inheritance}`);
      }
      existing.inheritance = definition.inheritance;
    }
    existing.members.push(...definition.members);
    definitions.set(definition.name, existing);
  }
}

if (!foundObsoleteHtmlIdl) {
  throw new Error("Could not find the obsolete HTML IDL block");
}

function extAttr(member, name) {
  return member.extAttrs?.find(attribute => attribute.name === name);
}

function reflectedName(member, reflect) {
  const rhs = reflect?.rhs;
  if (!rhs) {
    return member.name.toLowerCase();
  }
  if (typeof rhs.value === "string") {
    return rhs.value.replace(/^['\"]|['\"]$/g, "");
  }
  throw new Error(
      `Unsupported Reflect value for ${member.name}: ${JSON.stringify(rhs)}`);
}

function kotlinType(idlType) {
  if (!idlType || idlType.union || Array.isArray(idlType.idlType) || idlType.generic) {
    return "String";
  }
  const type = idlType.idlType;
  if (enums.has(type)) {
    return "String";
  }
  if (["DOMString", "USVString", "ByteString"].includes(type)) {
    return "String";
  }
  if (type === "boolean") {
    return "Boolean";
  }
  if (["byte", "octet", "short", "unsigned short", "long", "unsigned long"].includes(type)) {
    return "Int";
  }
  if (["long long", "unsigned long long"].includes(type)) {
    return "Long";
  }
  if (["float", "unrestricted float", "double", "unrestricted double"].includes(type)) {
    return "Double";
  }
  return "String";
}

function membersFor(name, seen = new Set()) {
  if (!name || seen.has(name)) {
    return [];
  }
  seen.add(name);
  const definition = definitions.get(name);
  if (!definition) {
    throw new Error(`Missing Web IDL definition for ${name}`);
  }
  return [
      ...membersFor(definition.inheritance, seen),
    ...(includes.get(name) ?? []).flatMap(mixin => membersFor(mixin, seen)),
    ...definition.members
  ];
}

function localMembersFor(name, seen = new Set()) {
  if (!name || seen.has(name)) {
    return [];
  }
  seen.add(name);
  const definition = definitions.get(name);
  if (!definition) {
    throw new Error(`Missing Web IDL definition for ${name}`);
  }
  return [
      ...(includes.get(name) ?? [])
      .flatMap(mixin => localMembersFor(mixin, seen)),
    ...definition.members
  ];
}

const globalAttributeProperties = new Set([
  "className",
  "draggable",
  "id",
  "style",
  "tabIndex",
  "title"
]);
const globalEventNames = new Set([
  "blur",
  "click",
  "focus",
  "focusin",
  "focusout",
  "keydown",
  "keyup",
  "load",
  "mousedown",
  "mouseenter",
  "mouseleave",
  "mousemove",
  "mouseout",
  "mouseover",
  "mouseup",
  "unload",
  "wheel"
]);

// Web IDL event-handler attributes are entirely lowercase, so their original word boundaries
// cannot easily be recovered procedurally
// Taken from React: 
// https://github.com/react/react/blob/900ae094d85b11c67d53dd14af50a2bda5db4495/packages/react-dom-bindings/src/events/DOMEventProperties.js#L40-L112
// Some names differ due to acronyms capitalization
const camelCasedEventNames = ["abort", "afterPrint", "animationCancel",
  "animationEnd", "animationIteration", "animationStart", "auxClick",
  "beforeInput", "beforeMatch", "beforePrint", "beforeToggle", "beforeUnload",
  "beforeXrSelect", "blur", "cancel", "canPlay", "canPlayThrough", "change",
  "click", "close", "command", "contextLost", "contextMenu", "contextRestored",
  "copy", "cueChange", "cut", "drag", "dragEnd", "dragEnter", "dragLeave",
  "dragOver", "dragStart", "drop", "durationChange", "emptied", "ended",
  "enterPictureInPicture", "error", "fencedTreeClick", "focus", "formData",
  "gamepadConnected", "gamepadDisconnected", "gotPointerCapture", "hashChange",
  "input", "invalid", "keyDown", "keyPress", "keyUp", "languageChange",
  "leavePictureInPicture", "load", "loadedData", "loadedMetadata", "loadStart",
  "location", "lostPointerCapture", "message", "messageError", "mouseDown",
  "mouseEnter", "mouseLeave", "mouseMove", "mouseOut", "mouseOver", "mouseUp",
  "offline", "online", "orientationChange", "pageHide", "pageReveal",
  "pageShow", "pageSwap", "paste", "pause", "play", "playing", "pointerCancel",
  "pointerDown", "pointerEnter", "pointerLeave", "pointerMove", "pointerOut",
  "pointerOver", "pointerRawUpdate", "pointerUp", "popState", "portalActivate",
  "progress", "promptAction", "promptDismiss", "rateChange", "rejectionHandled",
  "reset", "resize", "scroll", "scrollEnd", "securityPolicyViolation", "seeked",
  "seeking", "select", "selectionChange", "selectStart", "slotChange",
  "snapChanged", "snapChanging", "stalled", "storage", "submit", "suspend",
  "timeUpdate", "toggle", "touchCancel", "touchEnd", "touchMove", "touchStart",
  "transitionCancel", "transitionEnd", "transitionRun", "transitionStart",
  "unhandledRejection", "unload", "validationStatusChange", "volumeChange",
  "waiting", "webkitAnimationEnd", "webkitAnimationIteration",
  "webkitAnimationStart", "webkitTransitionEnd", "wheel"];

const eventParameterNames = new Map(
    camelCasedEventNames.map(
        name => [
            name.toLowerCase(),
          `on${name[0].toUpperCase()}${name.slice(1)}`
        ]));

eventParameterNames.set("dblclick", "onDoubleClick");

function eventParameterName(domEventName) {
  const parameter = eventParameterNames.get(domEventName);
  if (!parameter) {
    throw new Error(
        `Missing Kotlin parameter capitalization for event: ${domEventName}`);
  }
  return parameter;
}

const nonReflectingContentAttributes = new Map([
  ["className", "class"],
  ["href", "href"],
  ["src", "src"]
]);

function metadataFor(interfaceName, namespace) {
  const attributes = new Map();
  const events = new Map();
  const localMembers = new Set(localMembersFor(interfaceName));
  for (const member of membersFor(interfaceName)) {
    if (member.type !== "attribute") {
      continue;
    }
    const reflect = extAttr(member, "Reflect");
    const inferred = nonReflectingContentAttributes.get(member.name)
        ?? (namespace === "svg" && !member.name.startsWith("on") ? member.name : undefined);
    if ((reflect || inferred) && (localMembers.has(member) || globalAttributeProperties.has(member.name))) {
      const name = reflect ? reflectedName(member, reflect) : inferred;
      attributes.set(
          name,
          {
            name,
            parameter: member.name,
            type: kotlinType(member.idlType),
            obsolete: obsoleteMembers.has(member)
          });
    }
    if (member.name.startsWith("on")
        && (localMembers.has(member) 
            || globalEventNames.has(member.name.slice(2).toLowerCase()))) {
      const name = member.name.slice(2).toLowerCase();
      const parameter = eventParameterName(name);
      events.set(name, {name, parameter, type: "Event"});
    }
  }
  return {
    attributes: [...attributes.values()]
        .sort((a, b) => a.name.localeCompare(b.name)),
    events: [...events.values()]
        .sort((a, b) => a.name.localeCompare(b.name)),
  };
}

const voidHtml = new Set([
  "area",
  "base",
  "basefont",
  "bgsound",
  "br",
  "col",
  "embed",
  "frame",
  "hr",
  "img",
  "input",
  "keygen",
  "link",
  "meta",
  "param",
  "source",
  "track",
  "wbr"
]);

function namespaceFor(shortname) {
  if (/^svg/i.test(shortname) || ["css-masking-1", "filter-effects-1"].includes(shortname)) {
    return "svg";
  }
  if (/mathml/i.test(shortname)) {
    return "mathml";
  }
  return "html";
}

function shouldReplace(current, candidate) {
  if (!current) {
    return true;
  }
  if (current.obsolete !== candidate.obsolete) {
    return current.obsolete;
  }
  return !current.interface && Boolean(candidate.interface);
}

const merged = new Map();
const sortedElementSpecs = Object.entries(elementSpecs)
    .sort(([a], [b]) => a.localeCompare(b));
for (const [shortname, spec] of sortedElementSpecs) {
  const namespace = namespaceFor(shortname);
  for (const element of spec.elements) {
    const key = `${namespace}:${element.name}`;
    const candidate = {
      name: element.name,
      namespace,
      interface: element.interface ?? null,
      href: element.href,
      obsolete: element.obsolete === true,
      void: namespace === "html" && voidHtml.has(element.name),
      ...metadataFor(element.interface, namespace)
    };
    const current = merged.get(key);
    if (shouldReplace(current, candidate)) {
      merged.set(key, candidate);
    }
  }
}

const result = {
  source: {
    elements: "@webref/elements@2.7.1", idl: "@webref/idl@3.81.3",
  },
  elements: [...merged.values()]
      .sort((a, b) => a.namespace.localeCompare(b.namespace) || a.name.localeCompare(b.name)),
};

await mkdir(dirname(output), {recursive: true});
await writeFile(output, `${JSON.stringify(result, null, 2)}\n`);
