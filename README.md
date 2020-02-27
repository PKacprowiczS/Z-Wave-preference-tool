# Z-Wave device preferences generator
Simple Python script that allows to quickly create Z-Wave device preferences for SmartThings platform Groovy DTH basing on Product Data provided by a manufacturer. 

## Installation

Fastest way to install is cloning this repository.

```bash
git clone https://github.com/PKacprowiczS/Z-Wave-preference-tool.git
```

## Usage

### Generating preferences

Find device you're working on at [Z-Wave Products Catalog](https://products.z-wavealliance.org/) and download its Product Data in XML Format.

To generate preferences run

```bash
python3 ./preferences_generator.py --productData <path_to_XML_product_data>
```
### Template file

Template file contains 2 parts, which should be copied to different parts of DTH file.

"preferences" declaration (lines 1-66) must be copied to "metadata" part of DTH. 

Rest of template.groovy must be out of "metadata" part.

### Using output in DTH

Content of output file paste into your DTH.
