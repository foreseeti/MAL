" Vim syntax file
" Language: ;Meta Attack Language specification
" Creator: Pontus Johnson
" Latest Revision: 5 March 2019

if exists("b:current_syntax")
  finish
endif

syn keyword malKeyword abstractAsset include category asset extends info rationale associations

syntax match malComment "\v//.*$"
syntax region malString start=/"/ skip=/\\"/ end=/"/ oneline contains=malInterpolatedWrapper
syntax region malInterpolatedWrapper start="\v\\\(\s*" end="\v\s*\)" contained containedin=malString contains=malInterpolatedString
syntax match malInterpolatedString "\v\w+(\(\))?" contained containedin=malInterpolatedWrapper
syntax match malOperator  "\V,\|->\|& \|<--\|-->\|| \|E \|3 \|0-1\|*\|1-*\|\s1\s"
syntax match malIdentifier "\v\s[A-Z][_A-Za-z0-9]*"

highlight link malComment Comment
highlight default link malKeyword Keyword
highlight default link malString String
highlight default link malOperator Operator
highlight default link malIdentifier Function