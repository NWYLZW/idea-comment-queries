# IDEA Comment Queries

Powerful query type by comment.

## Usage

### Relative Query Rule

```typescript
type T = {
  a: string
  c?: boolean
  d: 'some desc\n\\n'
  e: {
    f: string
    g: true
  }
}

//   _?
type T0 = T['a']
//   ^?

type T2 = T['e']['f'] //<6?
//   ^?
//           ^2?
//                ^3?
```

## Command Options

### Below (supports optional line count)
 - `_?`
 - `⌄?`
 - `v?`
 - `V?`

### Above  (supports optional line count)
- `^?`

### Inline 
 - `<#?` - where `#` is the number of characters offset

## Related

Use in [VSCode](https://github.com/nwylzw/vscode-comment-queries#vscode-comment-queries-readme).
